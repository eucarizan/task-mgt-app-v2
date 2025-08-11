package dev.nj.tms.account;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AccountServiceTest {

    @Test
    void shouldCreateNewUserWithValidEmailAndPassword() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        when(accountRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountService accountService = new AccountService(accountRepository);

        String email = "user@example.com";
        String password = "secure123";

        Account account = accountService.register(email, password);

        assertNotNull(account);
        assertEquals(email, account.getEmail());
        assertNotNull(account.getPassword());
        verify(accountRepository).save(any(Account.class));
    }
}
