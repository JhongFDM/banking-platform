package com.group1.banking.security;

import com.group1.banking.entity.Account;
import com.group1.banking.entity.Customer;
import com.group1.banking.entity.User;
import com.group1.banking.enums.RoleName;
import com.group1.banking.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnershipServiceTest {

    @InjectMocks
    private OwnershipService ownershipService;

    @Mock
    private AccountRepository accountRepository;

    private CustomUserPrincipal customerPrincipal;
    private CustomUserPrincipal adminPrincipal;

    @BeforeEach
    void setUp() {
        User customerUser = new User();
        customerUser.setUserId(UUID.randomUUID());
        customerUser.setUsername("customer@example.com");
        customerUser.setPasswordHash("hash");
        customerUser.setRoles(new ArrayList<>(List.of(RoleName.RETAIL_CUSTOMER)));
        customerUser.setActive(true);
        customerUser.setCustomerId(42L);
        customerPrincipal = new CustomUserPrincipal(customerUser);

        User adminUser = new User();
        adminUser.setUserId(UUID.randomUUID());
        adminUser.setUsername("admin@example.com");
        adminUser.setPasswordHash("hash");
        adminUser.setRoles(new ArrayList<>(List.of(RoleName.BANK_ADMINISTRATOR)));
        adminUser.setActive(true);
        adminUser.setCustomerId(1L);
        adminPrincipal = new CustomUserPrincipal(adminUser);
    }

    private Authentication authOf(CustomUserPrincipal principal) {
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Test
    void canAccessCustomer_shouldReturnTrue_whenCustomerOwnsResource() {
        assertThat(ownershipService.canAccessCustomer(authOf(customerPrincipal), 42L)).isTrue();
    }

    @Test
    void canAccessCustomer_shouldReturnFalse_whenCustomerDoesNotOwnResource() {
        assertThat(ownershipService.canAccessCustomer(authOf(customerPrincipal), 99L)).isFalse();
    }

    @Test
    void canAccessCustomer_shouldReturnTrue_whenAdminAccessesAnyResource() {
        assertThat(ownershipService.canAccessCustomer(authOf(adminPrincipal), 99L)).isTrue();
    }

    @Test
    void canAccessCustomer_shouldReturnFalse_whenAuthenticationIsNull() {
        assertThat(ownershipService.canAccessCustomer(null, 42L)).isFalse();
    }

    @Test
    void canAccessCustomer_shouldReturnFalse_whenPrincipalIsNotCustomUserPrincipal() {
        Authentication auth = new UsernamePasswordAuthenticationToken("string-principal", null);
        assertThat(ownershipService.canAccessCustomer(auth, 42L)).isFalse();
    }

    @Test
    void canAccessCustomer_shouldReturnFalse_whenCustomerIdIsNull() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUsername("noid@example.com");
        user.setPasswordHash("hash");
        user.setRoles(new ArrayList<>(List.of(RoleName.RETAIL_CUSTOMER)));
        user.setActive(true);
        user.setCustomerId(null);
        CustomUserPrincipal principalWithNoCustomerId = new CustomUserPrincipal(user);
        assertThat(ownershipService.canAccessCustomer(authOf(principalWithNoCustomerId), 42L)).isFalse();
    }

    private Account accountOwnedBy(Long customerId) {
        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        Account account = new Account();
        account.setCustomer(customer);
        return account;
    }

    @Test
    void canAccessAccount_shouldReturnTrue_whenCustomerOwnsAccount() {
        when(accountRepository.findById(1001L)).thenReturn(Optional.of(accountOwnedBy(42L)));
        assertThat(ownershipService.canAccessAccount(authOf(customerPrincipal), 1001L)).isTrue();
    }

    @Test
    void canAccessAccount_shouldReturnFalse_whenCustomerDoesNotOwnAccount() {
        when(accountRepository.findById(1001L)).thenReturn(Optional.of(accountOwnedBy(99L)));
        assertThat(ownershipService.canAccessAccount(authOf(customerPrincipal), 1001L)).isFalse();
    }

    @Test
    void canAccessAccount_shouldReturnTrue_whenAdminAccessesAnyAccount() {
        assertThat(ownershipService.canAccessAccount(authOf(adminPrincipal), 1001L)).isTrue();
    }

    @Test
    void canAccessAccount_shouldReturnFalse_whenAccountNotFound() {
        when(accountRepository.findById(1001L)).thenReturn(Optional.empty());
        assertThat(ownershipService.canAccessAccount(authOf(customerPrincipal), 1001L)).isFalse();
    }

    @Test
    void canAccessAccount_shouldReturnFalse_whenAuthenticationIsNull() {
        assertThat(ownershipService.canAccessAccount(null, 1001L)).isFalse();
    }
}
