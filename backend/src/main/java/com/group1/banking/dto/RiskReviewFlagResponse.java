package com.group1.banking.dto;

import java.time.Instant;

public record RiskReviewFlagResponse(
        Long reviewId,
        Long customerId,
        String customerName,
        Long riskScoreId,
        Double riskScore,
        String primaryFactor,
        String flaggedBy,
        Instant createdAt,
        String status) {
}