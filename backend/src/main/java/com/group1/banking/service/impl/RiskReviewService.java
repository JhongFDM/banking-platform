package com.group1.banking.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group1.banking.dto.RiskReviewFlagResponse;
import com.group1.banking.dto.RiskScoreFactor;
import com.group1.banking.dto.RiskScoreResponse;
import com.group1.banking.entity.Customer;
import com.group1.banking.entity.RiskReviewFlag;
import com.group1.banking.entity.RiskScore;
import com.group1.banking.enums.RiskReviewStatus;
import com.group1.banking.enums.RiskScoreLevel;
import com.group1.banking.enums.RiskScoreStatus;
import com.group1.banking.exception.ConflictException;
import com.group1.banking.exception.NotFoundException;
import com.group1.banking.mapper.RiskScoreMapper;
import com.group1.banking.repository.CustomerRepository;
import com.group1.banking.repository.RiskReviewFlagRepository;
import com.group1.banking.repository.RiskScoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RiskReviewService {

    private final CustomerRepository customerRepository;
    private final RiskScoreRepository riskScoreRepository;
    private final RiskReviewFlagRepository reviewFlagRepository;
    private final RiskScoreMapper riskScoreMapper;

    @Transactional
    public RiskReviewFlagResponse flagForAdminReview(Long customerId, UUID analystUserId) {
        Customer customer = customerRepository.findByCustomerIdAndDeletedAtIsNull(customerId)
                .orElseThrow(() -> new NotFoundException("CUSTOMER_NOT_FOUND", "Customer not found", Map.of("customerId", customerId)));
        RiskScore latestScore = riskScoreRepository.findFirstByCustomerCustomerIdOrderByCalculatedAtDesc(customerId)
                .orElseThrow(() -> new ConflictException("RISK_SCORE_NOT_FOUND", "Customer has no saved risk score to flag", Map.of("customerId", customerId)));
        if (latestScore.getCalculateStatus() != RiskScoreStatus.OK || latestScore.getRiskLevel() != RiskScoreLevel.HIGH) {
            throw new ConflictException("CUSTOMER_NOT_HIGH_RISK", "Only customers with a current HIGH risk score can be flagged", Map.of("customerId", customerId));
        }
        if (reviewFlagRepository.existsByCustomerIdAndStatus(customerId, RiskReviewStatus.PENDING)) {
            throw new ConflictException("RISK_REVIEW_ALREADY_PENDING", "This customer is already flagged for admin review", Map.of("customerId", customerId));
        }

        RiskScoreResponse score = riskScoreMapper.toResponse(latestScore);
        RiskReviewFlag flag = new RiskReviewFlag();
        flag.setCustomerId(customerId);
        flag.setRiskScoreId(latestScore.getId());
        flag.setRiskScore(latestScore.getScore());
        flag.setPrimaryFactor(primaryFactor(score.getFactors()));
        flag.setFlaggedBy(analystUserId);
        flag.setStatus(RiskReviewStatus.PENDING);
        return toResponse(reviewFlagRepository.save(flag), customer.getName());
    }

    @Transactional(readOnly = true)
    public List<RiskReviewFlagResponse> getPendingReviews() {
        List<RiskReviewFlag> flags = reviewFlagRepository.findAllByStatusOrderByCreatedAtDesc(RiskReviewStatus.PENDING);
        Map<Long, String> customerNames = customerRepository.findAllById(
                flags.stream().map(flag -> flag.getCustomerId()).distinct().toList())
            .stream().collect(Collectors.toMap(customer -> customer.getCustomerId(), customer -> customer.getName()));
        return flags.stream().map(flag -> toResponse(flag,
                customerNames.getOrDefault(flag.getCustomerId(), "Deleted customer"))).toList();
    }

    @Transactional
    public RiskReviewFlagResponse markReviewed(Long reviewId, UUID adminUserId) {
        RiskReviewFlag flag = reviewFlagRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("RISK_REVIEW_NOT_FOUND", "Risk review not found", Map.of("reviewId", reviewId)));
        if (flag.getStatus() != RiskReviewStatus.PENDING) {
            throw new ConflictException("RISK_REVIEW_ALREADY_CLOSED", "Risk review has already been completed", Map.of("reviewId", reviewId));
        }
        flag.setStatus(RiskReviewStatus.REVIEWED);
        flag.setReviewedAt(Instant.now());
        flag.setReviewedBy(adminUserId);
        Customer customer = customerRepository.findById(flag.getCustomerId()).orElse(null);
        return toResponse(reviewFlagRepository.save(flag), customer == null ? "Deleted customer" : customer.getName());
    }

    private RiskReviewFlagResponse toResponse(RiskReviewFlag flag, String customerName) {
        return new RiskReviewFlagResponse(flag.getId(), flag.getCustomerId(), customerName,
                flag.getRiskScoreId(), flag.getRiskScore(), flag.getPrimaryFactor(),
                flag.getFlaggedBy().toString(), flag.getCreatedAt(), flag.getStatus().name());
    }

    private String primaryFactor(List<RiskScoreFactor> factors) {
        if (factors == null || factors.isEmpty()) {
            return "INSUFFICIENT_DATA";
        }
        return factors.stream().filter(factor -> factor.getContribution() != null)
                .max((left, right) -> Double.compare(left.getContribution(), right.getContribution()))
                .map(factor -> factor.getDataElement().name()).orElse("INSUFFICIENT_DATA");
    }
}