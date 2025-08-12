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

    @Test
    void shouldThrowConflictWhenEmailAlreadyExistsIgnoringCase() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        when(accountRepository.existsByEmailIgnoreCase(any())).thenReturn(true);

        AccountService accountService = new AccountService(accountRepository);

        String email = "User@Example.com";
        String password = "secure123";
        EmailAlreadyExistsException ex = assertThrows(
                EmailAlreadyExistsException.class,
                () -> accountService.register(email, password)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("email"));

        verify(accountRepository, never()).save(any(Account.class));
        verify(accountRepository).existsByEmailIgnoreCase(email);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenEmailIsNull() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        AccountService accountService = new AccountService(accountRepository);

        String password = "secure123";

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.register(null, password)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenEmailIsBlank() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        AccountService accountService = new AccountService(accountRepository);

        String password = "secure123";

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.register("   ", password)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenEmailIsEmpty() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        AccountService accountService = new AccountService(accountRepository);

        String password = "secure123";

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.register("", password)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenEmailHasInvalidFormat() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        AccountService accountService = new AccountService(accountRepository);

        String password = "secure123";

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.register("invalid-email", password)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenPasswordIsNull() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        AccountService accountService = new AccountService(accountRepository);

        String email = "user@example.com";

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.register(email, null)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("password"));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenPasswordIsBlank() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        AccountService accountService = new AccountService(accountRepository);

        String email = "user@example.com";

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.register(email, "   ")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("password"));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenPasswordIsEmpty() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        AccountService accountService = new AccountService(accountRepository);

        String email = "user@example.com";

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.register(email, "")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("password"));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenPasswordIsTooShort() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        AccountService accountService = new AccountService(accountRepository);

        String email = "user@example.com";

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.register(email, "12345")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("password"));
    }
}
