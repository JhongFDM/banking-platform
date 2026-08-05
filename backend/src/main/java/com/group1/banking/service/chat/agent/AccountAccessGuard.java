package com.group1.banking.service.chat.agent;

import com.group1.banking.entity.Account;
import com.group1.banking.exception.PermissionDeniedException;
import com.group1.banking.repository.AccountRepository;
import com.group1.banking.security.CustomUserPrincipal;
import org.springframework.stereotype.Component;

/**
 * Single, shared ownership check for every tool that touches account-scoped
 * data. Centralized on purpose: once a tool's arguments come from the LLM
 * rather than the client directly, this is the one place that has to be
 * right, and every account-touching tool routes through it instead of
 * re-implementing the same check.
 */
@Component
public class AccountAccessGuard {

    private final AccountRepository accountRepository;

    public AccountAccessGuard(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Resolves an account, preferring the model-supplied explicitAccountId
     * (untrusted) over defaultAccountId (the accountId from the original
     * chat request, already ownership-checked once by ChatService before
     * the agent loop started). Returns null if there's no account to
     * resolve either way -- callers treat that as "no account context."
     *
     * @throws PermissionDeniedException if the resolved account exists but
     *         does not belong to caller. This is intentionally NOT a soft
     *         failure: an ownership mismatch here means the model (acting
     *         on attacker-influenced input) tried to access another
     *         customer's data, so the whole chat turn aborts rather than
     *         feeding an error string back to the model and letting it
     *         retry with a different guess.
     */
    public Account resolve(Long explicitAccountId, Long defaultAccountId, CustomUserPrincipal caller) {
        Long targetId = explicitAccountId != null ? explicitAccountId : defaultAccountId;
        if (targetId == null) {
            return null;
        }

        Account account = accountRepository.findByAccountIdAndDeletedAtIsNull(targetId).orElse(null);
        if (account == null) {
            return null;
        }

        if (!account.getCustomer().getCustomerId().equals(caller.getCustomerId())) {
            throw new PermissionDeniedException("CHAT:AGENT_TOOL_ACCOUNT_OWNERSHIP");
        }

        return account;
    }
}
