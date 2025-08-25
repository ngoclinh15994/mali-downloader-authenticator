package com.malitool.authentication.service;

import com.malitool.authentication.dto.SubscriptionDTO;
import com.malitool.authentication.entity.User;
import com.malitool.authentication.entity.enums.UserStatus;
import com.malitool.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
public class SubscriptionService {

    @Autowired
    private UserRepository userRepository;

    public SubscriptionDTO getUserSubscription(User user) {
        if (user == null) {
            return null;
        }

        String planName = getPlanNameByStatus(user.getStatus());
        String status = user.getStatus().name();
        Date startDate = user.getCreatedDate();

        // Xác định subscription có active không
        boolean isActive = isSubscriptionActive(user);
        
        // Plus plan không có remaining days
        int remainingDays = -1; // Không có ngày hết hạn
        
        return new SubscriptionDTO(planName, status, startDate, null, isActive, remainingDays);
    }
    
    /**
     * Cập nhật subscription status cho user
     */
    public boolean updateUserSubscription(String userEmail, String planType, Date expirationDate) {
        try {
            User user = userRepository.findByEmail(userEmail);
            if (user == null) {
                return false;
            }

            user.setExpiredDate(null);
            if ("PLUS".equals(planType)) {
                user.setStatus(UserStatus.PLUS);
            } else {
                user.setStatus(UserStatus.FREE);
            }
            
            userRepository.save(user);
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    private String getPlanNameByStatus(UserStatus userStatus) {
        if (Objects.requireNonNull(userStatus) == UserStatus.PLUS) {
            return "Plus";
        }
        return "Free";
    }
    
    private boolean isSubscriptionActive(User user) {
        // Plus plan luôn active
        if (user.getStatus() == UserStatus.PLUS) {
            return true;
        }
        
        // Free plan cũng được coi là active
        if (user.getStatus() == UserStatus.FREE) {
            return true;
        }
        
        return false;
    }
}
