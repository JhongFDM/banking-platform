package com.group1.banking.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.group1.banking.entity.Account;
import com.group1.banking.repository.AccountRepository;

@Service("ownershipService")
public class OwnershipService {

    private final AccountRepository accountRepository;

    public OwnershipService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public boolean canAccessCustomer(Authentication authentication, Long customerId) {
        CustomUserPrincipal principal = principalOf(authentication);
        if (principal == null) {
            return false;
        }
        return isAdmin(principal)
                || (principal.getCustomerId() != null && principal.getCustomerId().equals(customerId));
    }

    /**
     * Same self-or-admin rule as {@link #canAccessCustomer}, but resolved from an
     * accountId rather than a customerId directly - for endpoints (like GET
     * /accounts/{accountId}) that only have the account in the path. A missing
     * account resolves to false rather than throwing, so callers using this from
     * @PreAuthorize get a clean 403 instead of a 500.
     */
    public boolean canAccessAccount(Authentication authentication, Long accountId) {
        CustomUserPrincipal principal = principalOf(authentication);
        if (principal == null) {
            return false;
        }
        if (isAdmin(principal)) {
            return true;
        }
        return accountRepository.findById(accountId)
                .map(Account::getCustomer)
                .map(customer -> customer.getCustomerId().equals(principal.getCustomerId()))
                .orElse(false);
    }

    private CustomUserPrincipal principalOf(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserPrincipal principal)) {
            return null;
        }
        return principal;
    }

    private boolean isAdmin(CustomUserPrincipal principal) {
        return principal.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_BANK_ADMINISTRATOR"));
    }
}
