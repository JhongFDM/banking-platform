package com.group1.banking.dto;

import java.time.Instant;
import java.util.List;

import com.group1.banking.enums.RiskScoreBand;
import com.group1.banking.enums.RiskScoreStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskScoreResponse {
    private Long customerId;
    private Double score;
    private RiskScoreBand band;
    private RiskScoreStatus status;
    private List<RiskScoreFactor> factors;
    private Instant calculatedAt;

}
