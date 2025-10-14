package dev.nj.tms.security;

import dev.nj.tms.account.Account;
import dev.nj.tms.account.AccountUserDetails;
import dev.nj.tms.token.AccessToken;
import dev.nj.tms.token.AccessTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.time.LocalDateTime;

public class AccessTokenAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(AccessTokenAuthenticationProvider.class);

    private final AccessTokenRepository tokenRepository;

    public AccessTokenAuthenticationProvider(AccessTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String token = (String) authentication.getCredentials();
        logger.info("Authenticating token: {}", token);

        logger.debug("Checking if token exists");
        AccessToken accessToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BadCredentialsException("Invalid token"));

        logger.debug("Checking if token is expired");
        if (accessToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Token expired");
        }

        Account account = accessToken.getAccount();

        AccountUserDetails userDetails = new AccountUserDetails(account);

        logger.debug("User is authenticated");
        return new BearerTokenAuthenticationToken(
                userDetails,
                token,
                userDetails.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
