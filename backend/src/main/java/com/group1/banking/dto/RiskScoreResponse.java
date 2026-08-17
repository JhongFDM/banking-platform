package com.group1.banking.dto;

import java.time.Instant;
import java.util.List;

import com.group1.banking.enums.RiskScoreLevel;
import com.group1.banking.enums.RiskScoreStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Null fields are omitted from the payload: when the status is
 * INSUFFICIENT_DATA there is no score, band or explanation to report, and the
 * contract requires those fields to be absent rather than present-and-null.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiskScoreResponse {
    private Long customerId;
    private Double score;
    private RiskScoreLevel level;
    private String explain;
    private RiskScoreStatus status; // enum: ok, insufficient_data
    private List<RiskScoreFactor> factors;
    private Instant calculatedAt;

    // Machine-readable code reusing the ErrorResponse convention, so callers
    // branch on a stable identifier instead of parsing prose. Only set when the
    // calculation could not produce a score.
    private String code;
    private String message;
}
