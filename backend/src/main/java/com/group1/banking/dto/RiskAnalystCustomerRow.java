package com.group1.banking.dto;

import com.group1.banking.enums.RiskScoreLevel;

public record RiskAnalystCustomerRow(
        Long customerId,
        Double riskScore,
        RiskScoreLevel level,
        Double trend,
        String primaryFactor,
        String action,
        boolean insufficientData,
        boolean reviewFlagged) {
}