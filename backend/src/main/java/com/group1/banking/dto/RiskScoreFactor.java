package com.group1.banking.dto;

import com.group1.banking.enums.RiskScoreDataElement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiskScoreFactor {
    private RiskScoreDataElement dataElement; // enum: spending/income ratio, saving balance, saving goals
    private Double subscore;
    private Double weight;
    private Double contribution;
}
