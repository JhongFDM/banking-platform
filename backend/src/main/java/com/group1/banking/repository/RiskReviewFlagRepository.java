package com.group1.banking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.group1.banking.entity.RiskReviewFlag;
import com.group1.banking.enums.RiskReviewStatus;

public interface RiskReviewFlagRepository extends JpaRepository<RiskReviewFlag, Long> {
    boolean existsByCustomerIdAndStatus(Long customerId, RiskReviewStatus status);

    List<RiskReviewFlag> findAllByStatusOrderByCreatedAtDesc(RiskReviewStatus status);
}