package com.group1.banking.entity;

import java.time.Instant;
import java.util.UUID;

import com.group1.banking.enums.RiskReviewStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "risk_review_flags")
@Getter
@Setter
@NoArgsConstructor
public class RiskReviewFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "risk_score_id", nullable = false)
    private Long riskScoreId;

    @Column(name = "risk_score", nullable = false)
    private Double riskScore;

    @Column(name = "primary_factor", nullable = false, length = 100)
    private String primaryFactor;

    @Column(name = "flagged_by", nullable = false)
    private UUID flaggedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskReviewStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @PrePersist
    public void onCreate() {
        createdAt = Instant.now();
        if (status == null) {
            status = RiskReviewStatus.PENDING;
        }
    }
}