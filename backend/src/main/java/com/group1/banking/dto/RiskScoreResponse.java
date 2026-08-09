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
    private RiskScoreBand band; // enum: low, medium, high
    private RiskScoreStatus status; // enum: ok, insufficient_data
    private List<RiskScoreFactor> factors;
    private Instant calculatedAt;

}
