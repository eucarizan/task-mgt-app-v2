package dev.nj.tms.token;

import dev.nj.tms.account.Account;
import dev.nj.tms.account.AccountMapper;
import dev.nj.tms.account.AccountRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        when(accountRepository.findByEmailIgnoreCase(email))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

        String token = tokenService.createToken(email, password);

        assertNotNull(token);
        assertTrue(token.length() >= 10);

        verify(accountRepository).findByEmailIgnoreCase(email);
        verify(passwordEncoder).matches(password, encodedPassword);
        verify(tokenRepository, atLeastOnce()).save(any(AccessToken.class));
    }
}
