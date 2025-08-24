package com.malitool.authentication.service;

import com.malitool.authentication.dto.SubscriptionDTO;
import com.malitool.authentication.entity.User;
import com.malitool.authentication.entity.enums.UserStatus;
import com.malitool.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class SubscriptionService {

    @Autowired
    private UserRepository userRepository;

    public SubscriptionDTO getUserSubscription(User user) {
        if (user == null) {
            return null;
        }

        // Xác định plan name dựa trên status
        String planName = getPlanNameByStatus(user.getStatus());
        
        // Xác định subscription status
        String status = user.getStatus().name();
        
        // Xác định start date (created date)
        Date startDate = user.getCreatedDate();
        
        // Plus plan không có expired date, chỉ có Free mới có thể có
        Date endDate = null; // Luôn null vì Plus plan không có hết hạn
        
        // Xác định subscription có active không
        boolean isActive = isSubscriptionActive(user);
        
        // Plus plan không có remaining days
        int remainingDays = -1; // Không có ngày hết hạn
        
        return new SubscriptionDTO(planName, status, startDate, endDate, isActive, remainingDays);
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
            
            // Chỉ có Plus plan
            if ("PLUS".equals(planType)) {
                user.setStatus(UserStatus.PLUS);
                // Plus plan không có kỳ hạn nên luôn set null
                user.setExpiredDate(null);
            } else {
                // Fallback về Free
                user.setStatus(UserStatus.FREE);
                user.setExpiredDate(null);
            }
            
            userRepository.save(user);
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    private String getPlanNameByStatus(UserStatus userStatus) {
        switch (userStatus) {
            case PLUS: return "Plus";
            case FREE: return "Free";
            default: return "Free";
        }
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
    
    private int calculateRemainingDays(Date endDate) {
        // Không có remaining days vì Plus plan không có hết hạn
        return -1; // Luôn trả về -1 (unlimited)
    }
}
