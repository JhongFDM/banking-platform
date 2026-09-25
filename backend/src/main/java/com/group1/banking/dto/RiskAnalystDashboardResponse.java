package com.group1.banking.dto;

import java.util.List;

public record RiskAnalystDashboardResponse(
        long totalFlaggedAccounts,
        Double averageRiskScore,
        double systemConfidence,
        long customersWithCompleteData,
        long totalCustomers,
        List<RiskAnalystCustomerRow> customers) {
}