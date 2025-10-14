package dev.nj.tms.security;

import dev.nj.tms.account.Account;
import dev.nj.tms.token.AccessToken;
import dev.nj.tms.token.AccessTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccessTokenAuthenticationProviderTest {

    @Mock
    private AccessTokenRepository tokenRepository;

    @InjectMocks
    private AccessTokenAuthenticationProvider provider;

    @Test
    void authenticate_validToken_returnsAuthentication() {
        String tokenValue = "valid-token-123";
        Account account = new Account("user@mail.com", "secureP1");
        AccessToken token = new AccessToken(tokenValue, account, LocalDateTime.now().plusHours(1));

        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

        Authentication result = provider.authenticate(new BearerTokenAuthenticationToken(tokenValue));

        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        assertEquals("user@mail.com", ((UserDetails) result.getPrincipal()).getUsername());
    }

    @Test
    void authenticate_expiredToken_throwsException() {
        String tokenValue = "expired-token-123";
        Account account = new Account("user@mail.com", "secureP1");
        AccessToken token = new AccessToken(tokenValue, account, LocalDateTime.now().minusHours(1));

        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

        assertThrows(BadCredentialsException.class, () ->
                provider.authenticate(new BearerTokenAuthenticationToken(tokenValue)));
    }

    @Test
    void authenticate_invalidToken_throwsException() {
        when(tokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () ->
                provider.authenticate(new BearerTokenAuthenticationToken("invalid-token")));
    }

    @Test
    void supports_bearerTokenAuthenticationToken_returnsTrue() {
        assertTrue(provider.supports(BearerTokenAuthenticationToken.class));
    }
}
