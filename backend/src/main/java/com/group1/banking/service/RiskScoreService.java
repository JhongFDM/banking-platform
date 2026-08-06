package com.group1.banking.service;

import org.springframework.stereotype.Service;

import com.group1.banking.dto.RiskScoreResponse;
import com.group1.banking.repository.RiskScoreRepository;
import com.group1.banking.security.CustomUserPrincipal;

@Service
public class RiskScoreService {
    private final RiskScoreRepository riskScoreRepository;

    public RiskScoreService(RiskScoreRepository riskScoreRepository) {
        this.riskScoreRepository = riskScoreRepository;
    }

    @org.springframework.transaction.annotation.Transactional
    public RiskScoreResponse calculateRiskScore(Long customer_id) {
        return null;
    }

    public RiskScoreResponse getRiskScoreById(Long customer_id, CustomUserPrincipal principal) {
        return null;
    }
}
