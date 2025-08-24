package com.malitool.authentication.dto;

import java.util.Date;

public record SubscriptionDTO(
        String planName,
        String status,
        Date startDate,
        Date endDate,
        boolean isActive,
        int remainingDays
) {
}

