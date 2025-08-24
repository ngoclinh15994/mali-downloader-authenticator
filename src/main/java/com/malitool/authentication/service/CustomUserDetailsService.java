package com.malitool.authentication.service;

import com.malitool.authentication.dto.RegisterRequest;
import com.malitool.authentication.entity.User;
import com.malitool.authentication.entity.enums.UserStatus;
import com.malitool.authentication.repository.UserRepository;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomUserDetailsService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        List<String> roles = new ArrayList<>();
        roles.add("USER");
        return
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .roles(roles.toArray(new String[0]))
                        .build();
    }

    public void createNewUserFrom(RegisterRequest registerUserRequestDTO) {
        User user = getUserFrom(registerUserRequestDTO);
        if(isValidUser(user)){
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("Email already in use");
        }
    }

    public User getUserFrom(RegisterRequest registerUserRequestDTO) {
        User user = new User();
        user.setEmail(registerUserRequestDTO.getEmail());
        user.setUsername(registerUserRequestDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerUserRequestDTO.getPassword()));
        user.setCreatedDate(new Date());
        user.setExpiredDate(DateUtils.addDays(new Date(), 7));
        user.setStatus(UserStatus.FREE);
        return user;
    }

    public boolean isValidUser(User user) {
        User existedUser = userRepository.findByEmail(user.getEmail());
        return existedUser == null;
    }
}
