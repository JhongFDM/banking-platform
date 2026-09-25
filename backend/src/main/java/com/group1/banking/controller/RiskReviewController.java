package com.group1.banking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.group1.banking.dto.RiskReviewFlagResponse;
import com.group1.banking.security.CustomUserPrincipal;
import com.group1.banking.service.impl.RiskReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RiskReviewController {

    private final RiskReviewService riskReviewService;

    @PostMapping("/api/risk-analyst/reviews/customers/{customerId}")
    @PreAuthorize("hasRole('RISK_ANALYST')")
    @ResponseStatus(HttpStatus.CREATED)
    public RiskReviewFlagResponse flagForAdminReview(
            @PathVariable Long customerId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return riskReviewService.flagForAdminReview(customerId, principal.getUserId());
    }

    @GetMapping("/api/admin/risk-reviews")
    @PreAuthorize("hasRole('BANK_ADMINISTRATOR')")
    public List<RiskReviewFlagResponse> getPendingReviews() {
        return riskReviewService.getPendingReviews();
    }

    @PostMapping("/api/admin/risk-reviews/{reviewId}/resolve")
    @PreAuthorize("hasRole('BANK_ADMINISTRATOR')")
    public RiskReviewFlagResponse markReviewed(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return riskReviewService.markReviewed(reviewId, principal.getUserId());
    }
}