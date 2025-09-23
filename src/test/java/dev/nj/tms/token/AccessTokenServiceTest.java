package dev.nj.tms.token;

import dev.nj.tms.account.Account;
import dev.nj.tms.account.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccessTokenServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccessTokenRepository tokenRepository;

    @InjectMocks
    private AccessTokenServiceImpl tokenService;

    @Test
    void createToken_validCredentials_returnsToken() {
        String email = "user1@mail.com";
        String password = "secureP1";
        String encodedPassword = "$2a$10$encodedPassword";

        Account account = new Account(email, encodedPassword);
        when(accountRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

        AccessTokenResponse token = tokenService.createToken(email, password);

        assertNotNull(token);
        assertTrue(token.token().length() >= 10);

        verify(accountRepository).findByEmailIgnoreCase(email);
        verify(passwordEncoder).matches(password, encodedPassword);
        verify(tokenRepository, atLeastOnce()).save(any(AccessToken.class));
    }

    @Test
    void createToken_unknownUser_throwsUnauthorized() {
        String email = "test@mail.com";

        when(accountRepository.findByEmailIgnoreCase(email))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> tokenService.createToken(email, "secureP1"));

        assertTrue(exception.getMessage().contains("Invalid credentials"));
    }

    @Test
    void createToken_wrongPassword_throwsUnauthorized() {
        String email = "user1@mail.com";
        String password = "wrongPass1";
        String encodedPassword = "$2a$10$encodedPassword";

        Account account = new Account(email, encodedPassword);
        when(accountRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> tokenService.createToken(email, password));

        assertTrue(exception.getMessage().contains("Invalid credentials"));
    }
}
