package com.group1.banking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.group1.banking.entity.RiskScore;

@Repository
public interface RiskScoreRepository extends JpaRepository<RiskScore, Long> {

}
