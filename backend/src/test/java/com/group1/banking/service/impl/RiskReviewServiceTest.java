package com.group1.banking.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.group1.banking.dto.RiskScoreResponse;
import com.group1.banking.entity.Customer;
import com.group1.banking.entity.RiskReviewFlag;
import com.group1.banking.entity.RiskScore;
import com.group1.banking.enums.RiskReviewStatus;
import com.group1.banking.enums.RiskScoreLevel;
import com.group1.banking.enums.RiskScoreStatus;
import com.group1.banking.exception.ConflictException;
import com.group1.banking.mapper.RiskScoreMapper;
import com.group1.banking.repository.CustomerRepository;
import com.group1.banking.repository.RiskReviewFlagRepository;
import com.group1.banking.repository.RiskScoreRepository;

class RiskReviewServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private RiskScoreRepository riskScoreRepository;
    @Mock
    private RiskReviewFlagRepository reviewFlagRepository;
    @Mock
    private RiskScoreMapper riskScoreMapper;

    private RiskReviewService service;
    private Customer customer;
    private RiskScore score;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new RiskReviewService(customerRepository, riskScoreRepository,
                reviewFlagRepository, riskScoreMapper);
        customer = new Customer();
        customer.setCustomerId(17L);
        customer.setName("Test Customer");
        score = new RiskScore();
        score.setId(81L);
        score.setScore(82.5);
        score.setRiskLevel(RiskScoreLevel.HIGH);
        score.setCalculateStatus(RiskScoreStatus.OK);
        when(customerRepository.findByCustomerIdAndDeletedAtIsNull(17L)).thenReturn(Optional.of(customer));
        when(riskScoreRepository.findFirstByCustomerCustomerIdOrderByCalculatedAtDesc(17L))
                .thenReturn(Optional.of(score));
        when(riskScoreMapper.toResponse(score)).thenReturn(RiskScoreResponse.builder().build());
        when(reviewFlagRepository.existsByCustomerIdAndStatus(17L, RiskReviewStatus.PENDING)).thenReturn(false);
        when(reviewFlagRepository.save(any(RiskReviewFlag.class))).thenAnswer(invocation -> {
            RiskReviewFlag flag = invocation.getArgument(0);
            flag.setId(5L);
            flag.setCreatedAt(Instant.parse("2026-09-25T10:00:00Z"));
            return flag;
        });
    }

    @Test
    void flagForAdminReview_persistsHighRiskSnapshotAndAnalyst() {
        UUID analystId = UUID.randomUUID();

        var result = service.flagForAdminReview(17L, analystId);

        assertThat(result.reviewId()).isEqualTo(5L);
        assertThat(result.customerId()).isEqualTo(17L);
        assertThat(result.customerName()).isEqualTo("Test Customer");
        assertThat(result.riskScore()).isEqualTo(82.5);
        assertThat(result.flaggedBy()).isEqualTo(analystId.toString());
        assertThat(result.status()).isEqualTo("PENDING");
        verify(reviewFlagRepository).save(any(RiskReviewFlag.class));
    }

    @Test
    void flagForAdminReview_rejectsNonHighScore() {
        score.setRiskLevel(RiskScoreLevel.ELEVATED);

        assertThatThrownBy(() -> service.flagForAdminReview(17L, UUID.randomUUID()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Only customers with a current HIGH risk score");
        verify(reviewFlagRepository, never()).save(any(RiskReviewFlag.class));
    }

    @Test
    void flagForAdminReview_rejectsDuplicatePendingFlag() {
        when(reviewFlagRepository.existsByCustomerIdAndStatus(17L, RiskReviewStatus.PENDING)).thenReturn(true);

        assertThatThrownBy(() -> service.flagForAdminReview(17L, UUID.randomUUID()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already flagged");
        verify(reviewFlagRepository, never()).save(any(RiskReviewFlag.class));
    }
}