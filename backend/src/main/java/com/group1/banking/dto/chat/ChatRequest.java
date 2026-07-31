package com.group1.banking.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * POST /api/chat request body. customerId is deliberately NOT a field here --
 * it is always resolved server-side from the authenticated principal, the
 * same pattern used everywhere else in this codebase for ownership-scoped
 * data (see SavingsGoalController). Never trust a client-supplied customer
 * or ownership identifier.
 */
public class ChatRequest {

    @NotBlank(message = "Message cannot be empty")
    @Size(max = 500, message = "Message must be 500 characters or fewer")
    private String message;

    /**
     * Optional. When present, the assistant scopes transaction/goal context
     * to this account (after verifying it belongs to the caller). When
     * absent, the assistant answers generically without account-specific data.
     */
    private Long accountId;

    public ChatRequest() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
