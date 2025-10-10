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
        String encodedPassword = "$2a$10$encodedPassword";

        Account account = new Account(email, encodedPassword);
        when(accountRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(account));

        AccessTokenResponse token = tokenService.createToken(email);

        assertNotNull(token);
        assertTrue(token.token().length() >= 10);

        verify(accountRepository).findByEmailIgnoreCase(email);
        verify(tokenRepository, atLeastOnce()).save(any(AccessToken.class));
    }

    @Test
    void createToken_unknownUser_throwsUnauthorized() {
        String email = "test@mail.com";

        when(accountRepository.findByEmailIgnoreCase(email))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> tokenService.createToken(email));

        assertTrue(exception.getMessage().contains("Invalid credentials"));
    }
}
