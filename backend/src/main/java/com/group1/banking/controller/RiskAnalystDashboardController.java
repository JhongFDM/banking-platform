package com.group1.banking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group1.banking.dto.RiskAnalystDashboardResponse;
import com.group1.banking.service.impl.RiskAnalystDashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/risk-analyst/dashboard")
@RequiredArgsConstructor
public class RiskAnalystDashboardController {

    private final RiskAnalystDashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasRole('RISK_ANALYST')")
    public ResponseEntity<RiskAnalystDashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }
}