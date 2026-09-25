package com.group1.banking.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import com.group1.banking.dto.accountcontrol.FreezeAccountRequest;
import com.group1.banking.dto.accountcontrol.UnfreezeAccountRequest;
import com.group1.banking.security.CustomUserPrincipal;

class RiskReviewControllerTest {

    @Test
    void analystCanFlagButOnlyAdminCanViewOrResolveQueue() throws NoSuchMethodException {
        Method flag = RiskReviewController.class.getDeclaredMethod("flagForAdminReview", Long.class,
                CustomUserPrincipal.class);
        Method list = RiskReviewController.class.getDeclaredMethod("getPendingReviews");
        Method resolve = RiskReviewController.class.getDeclaredMethod("markReviewed", Long.class,
                CustomUserPrincipal.class);

        assertThat(flag.getAnnotation(PreAuthorize.class).value()).isEqualTo("hasRole('RISK_ANALYST')");
        assertThat(list.getAnnotation(PreAuthorize.class).value()).isEqualTo("hasRole('BANK_ADMINISTRATOR')");
        assertThat(resolve.getAnnotation(PreAuthorize.class).value()).isEqualTo("hasRole('BANK_ADMINISTRATOR')");
    }

    @Test
    void freezeAndUnfreezeRemainAdminOnly() throws NoSuchMethodException {
        Method freeze = AccountController.class.getDeclaredMethod("freezeAccount", Long.class, FreezeAccountRequest.class);
        Method unfreeze = AccountController.class.getDeclaredMethod("unfreezeAccount", Long.class, UnfreezeAccountRequest.class);

        assertThat(freeze.getAnnotation(PreAuthorize.class).value()).isEqualTo("hasRole('BANK_ADMINISTRATOR')");
        assertThat(unfreeze.getAnnotation(PreAuthorize.class).value()).isEqualTo("hasRole('BANK_ADMINISTRATOR')");
    }
}