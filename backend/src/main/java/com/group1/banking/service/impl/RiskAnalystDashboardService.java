package com.group1.banking.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group1.banking.dto.RiskAnalystCustomerRow;
import com.group1.banking.dto.RiskAnalystDashboardResponse;
import com.group1.banking.dto.RiskScoreFactor;
import com.group1.banking.dto.RiskScoreResponse;
import com.group1.banking.entity.Customer;
import com.group1.banking.entity.RiskScore;
import com.group1.banking.enums.RiskScoreLevel;
import com.group1.banking.enums.RiskScoreStatus;
import com.group1.banking.enums.RiskReviewStatus;
import com.group1.banking.mapper.RiskScoreMapper;
import com.group1.banking.repository.CustomerRepository;
import com.group1.banking.repository.RiskReviewFlagRepository;
import com.group1.banking.repository.RiskScoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RiskAnalystDashboardService {

    private static final String INSUFFICIENT_DATA = "INSUFFICIENT_DATA";

    private final CustomerRepository customerRepository;
    private final RiskScoreRepository riskScoreRepository;
    private final RiskReviewFlagRepository reviewFlagRepository;
    private final RiskScoreMapper riskScoreMapper;

    @Transactional(readOnly = true)
    public RiskAnalystDashboardResponse getDashboard() {
        List<Customer> customers = customerRepository.findAllByDeletedAtIsNullOrderByCustomerIdAsc();
        Map<Long, List<RiskScore>> scoresByCustomer = groupScoresByCustomer(riskScoreRepository.findAllByOrderByCustomerCustomerIdAscCalculatedAtDesc());
        Set<Long> flaggedForReview = reviewFlagRepository.findAllByStatusOrderByCreatedAtDesc(RiskReviewStatus.PENDING)
            .stream().map(flag -> flag.getCustomerId()).collect(Collectors.toSet());

        List<RiskAnalystCustomerRow> rows = new ArrayList<>();
        double scoreTotal = 0;
        long scoredCustomers = 0;
        long flaggedCustomers = 0;
        long completeCustomers = 0;

        for (Customer customer : customers) {
            List<RiskScore> history = scoresByCustomer.getOrDefault(customer.getCustomerId(), List.of());
                RiskAnalystCustomerRow row = toRow(customer.getCustomerId(), history,
                    flaggedForReview.contains(customer.getCustomerId()));
            rows.add(row);

            if (!row.insufficientData()) {
                scoreTotal += row.riskScore();
                scoredCustomers++;
                if (row.level() == RiskScoreLevel.HIGH) {
                    flaggedCustomers++;
                }
                if (isCompleteData(history.get(0))) {
                    completeCustomers++;
                }
            }
        }

        double confidence = customers.isEmpty() ? 0 : percentage(completeCustomers, customers.size());
        Double average = scoredCustomers == 0 ? null : round(scoreTotal / scoredCustomers);

        return new RiskAnalystDashboardResponse(
                flaggedCustomers,
                average,
                confidence,
                completeCustomers,
                customers.size(),
                rows);
    }

    private Map<Long, List<RiskScore>> groupScoresByCustomer(List<RiskScore> scores) {
        Map<Long, List<RiskScore>> grouped = new HashMap<>();
        for (RiskScore score : scores) {
            grouped.computeIfAbsent(score.getCustomer().getCustomerId(), ignored -> new ArrayList<>()).add(score);
        }
        return grouped;
    }

    private RiskAnalystCustomerRow toRow(Long customerId, List<RiskScore> history, boolean reviewFlagged) {
        if (history.isEmpty()) {
            return new RiskAnalystCustomerRow(customerId, null, null, null, INSUFFICIENT_DATA,
                    "VIEW_DETAILS", true, reviewFlagged);
        }

        RiskScoreResponse latest = riskScoreMapper.toResponse(history.get(0));
        if (latest.getCalculateStatus() != RiskScoreStatus.OK || latest.getScore() == null) {
            return new RiskAnalystCustomerRow(latest.getCustomerId(), null, null, null,
                    INSUFFICIENT_DATA, "VIEW_DETAILS", true, reviewFlagged);
        }

        Double trend = history.size() > 1
                ? round(latest.getScore() - history.get(1).getScore())
                : null;
        return new RiskAnalystCustomerRow(
                latest.getCustomerId(),
                round(latest.getScore()),
                latest.getLevel(),
                trend,
                primaryFactor(latest.getFactors()),
                actionFor(latest.getLevel(), reviewFlagged),
                false,
                reviewFlagged);
    }

    private String primaryFactor(List<RiskScoreFactor> factors) {
        if (factors == null || factors.isEmpty()) {
            return INSUFFICIENT_DATA;
        }
        return factors.stream()
                .filter(factor -> factor.getContribution() != null)
            .max(Comparator.comparing(factor -> factor.getContribution()))
                .map(factor -> factor.getDataElement().name())
                .orElse(INSUFFICIENT_DATA);
    }

    private boolean isCompleteData(RiskScore score) {
        List<RiskScoreFactor> factors = riskScoreMapper.toResponse(score).getFactors();
        return factors != null && !factors.isEmpty() && factors.stream().allMatch(factor -> factor.isValid());
    }

    private String actionFor(RiskScoreLevel level, boolean reviewFlagged) {
        return level == RiskScoreLevel.HIGH ? (reviewFlagged ? "REVIEW_FLAGGED" : "FLAG_FOR_ADMIN_REVIEW") :
                level == RiskScoreLevel.ELEVATED ? "INVESTIGATE" : "VIEW_DETAILS";
    }

    private double percentage(long numerator, long denominator) {
        return round((numerator * 100.0) / denominator);
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}