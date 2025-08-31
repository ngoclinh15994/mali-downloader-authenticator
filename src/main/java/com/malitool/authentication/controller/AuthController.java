package com.malitool.authentication.controller;

import com.malitool.authentication.dto.ErrorResponse;
import com.malitool.authentication.dto.LoginRequest;
import com.malitool.authentication.dto.LoginResponse;
import com.malitool.authentication.dto.RegisterRequest;
import com.malitool.authentication.dto.SubscriptionDTO;
import com.malitool.authentication.entity.User;
import com.malitool.authentication.repository.UserRepository;
import com.malitool.authentication.service.CustomUserDetailsService;
import com.malitool.authentication.service.SubscriptionService;
import com.malitool.authentication.util.JwtUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"https://malitool.com", "https://malicoder.com", "https://promptgetter.com", "http://localhost:5173", "https://portal.malitool.com"},
        originPatterns = {"chrome-extension://*"}, maxAge = 3600)
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                          CustomUserDetailsService customUserDetailsService, SubscriptionService subscriptionService,
                          UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
        this.subscriptionService = subscriptionService;
        this.userRepository = userRepository;
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginReq) {
        try {
            Authentication authentication =
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginReq.getEmail(), loginReq.getPassword()));
            String email = authentication.getName();
            String token = jwtUtil.createToken(loginReq);

            // Lấy thông tin user để tạo subscription info
            User user = userRepository.findByEmail(email);
            SubscriptionDTO subscription = subscriptionService.getUserSubscription(user);

            return ResponseEntity.ok(new LoginResponse(email, token, subscription));

        } catch (BadCredentialsException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    List.of("Invalid username or password")
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, List.of(e.getMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping(value = "/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        customUserDetailsService.createNewUserFrom(registerRequest);
        return ResponseEntity.ok(HttpEntity.EMPTY);
    }

    @PostMapping(value = "/verifyToken")
    public ResponseEntity<?> verifyToken() {
        try {
            UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            // Lấy thông tin user đầy đủ từ database
            User user = userRepository.findByEmail(email);
            if (user != null) {
                SubscriptionDTO subscription = subscriptionService.getUserSubscription(user);
                return ResponseEntity.ok(new LoginResponse(email, null, subscription));
            } else {
                return ResponseEntity.badRequest().body("User not found");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error verifying token: " + e.getMessage());
        }
    }

    @GetMapping(value = "/profile")
    public ResponseEntity<?> getProfile() {
        try {
            UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            // Lấy thông tin user đầy đủ từ database
            User user = userRepository.findByEmail(email);
            if (user != null) {
                SubscriptionDTO subscription = subscriptionService.getUserSubscription(user);
                
                // Tạo response object với thông tin chi tiết
                Map<String, Object> profile = Map.of(
                    "email", user.getEmail(),
                    "username", user.getUsername(),
                    "phoneNumber", user.getPhoneNumber(),
                    "status", user.getStatus(),
                    "createdDate", user.getCreatedDate(),
                    "subscription", subscription
                );
                
                return ResponseEntity.ok(profile);
            } else {
                return ResponseEntity.badRequest().body("User not found");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error getting profile: " + e.getMessage());
        }
    }

    @PostMapping(value = "/subscription/update")
    public ResponseEntity<?> updateSubscription(@RequestBody Map<String, Object> request) {
        try {
            String userEmail = (String) request.get("userEmail");
            String planType = (String) request.get("planType");
            Object expirationDateObj = request.get("expirationDate");

            Date expirationDate = null;
            if (expirationDateObj != null && !"null".equals(expirationDateObj.toString())) {
                try {
                    long timestamp = Long.parseLong(expirationDateObj.toString());
                    expirationDate = new Date(timestamp);
                } catch (NumberFormatException e) {
                    // Ignore invalid date format
                }
            }

            boolean success = subscriptionService.updateUserSubscription(userEmail, planType, expirationDate);

            if (success) {
                return ResponseEntity.ok().body("Subscription updated successfully");
            } else {
                return ResponseEntity.badRequest().body("Failed to update subscription");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating subscription: " + e.getMessage());
        }
    }
}
