package com.group1.banking.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import com.group1.banking.security.CustomUserPrincipal;

class RiskScoreControllerTest {

    @Test
    void riskReadMethods_shouldAllowRiskAnalyst() throws NoSuchMethodException {
        Method getRiskScore = RiskScoreController.class.getDeclaredMethod("getRiskScore", Long.class,
                CustomUserPrincipal.class);
        Method getHistory = RiskScoreController.class.getDeclaredMethod("getRiskScoreHistory", Long.class,
                CustomUserPrincipal.class);
        Method calculateRiskScore = RiskScoreController.class.getDeclaredMethod("calculateRiskScore", Long.class,
                CustomUserPrincipal.class);

        assertThat(getRiskScore.getAnnotation(PreAuthorize.class).value())
                .contains("RISK_ANALYST");
        assertThat(getHistory.getAnnotation(PreAuthorize.class).value())
                .contains("RISK_ANALYST");
        assertThat(calculateRiskScore.getAnnotation(PreAuthorize.class).value())
                .doesNotContain("RISK_ANALYST");
    }
}
