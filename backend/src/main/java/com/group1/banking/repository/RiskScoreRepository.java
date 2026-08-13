package com.group1.banking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.group1.banking.entity.RiskScore;

public interface RiskScoreRepository extends JpaRepository<RiskScore, Long> {

}
