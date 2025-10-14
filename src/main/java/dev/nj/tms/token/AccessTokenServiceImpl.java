package dev.nj.tms.token;

import dev.nj.tms.account.Account;
import dev.nj.tms.account.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Service
public class AccessTokenServiceImpl implements AccessTokenService {

    private static final Logger logger = LoggerFactory.getLogger(AccessTokenServiceImpl.class);

    private final AccessTokenRepository tokenRepository;
    private final AccountRepository accountRepository;

    public AccessTokenServiceImpl(AccessTokenRepository tokenRepository, AccountRepository accountRepository) {
        this.tokenRepository = tokenRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public AccessTokenResponse createToken(String email) {
        logger.debug("Attempting to create token for email: {}", email);
        Account account = accountRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    logger.warn("Account not found for email: {}", email);
                    return new IllegalArgumentException("Invalid credentials");
                });
        logger.debug("Account found for email: {}", email);

        byte[] bytes = KeyGenerators.secureRandom(10).generateKey();
        String tokenValue = new BigInteger(1, bytes).toString(16);
        logger.debug("Generated new access token for email: {}", email);

        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
        AccessToken token = new AccessToken(tokenValue, account, expiresAt);

        tokenRepository.save(token);
        logger.debug("Persisted new access token for email: {}", email);

        logger.info("Access token created successfully for email: {}", email);
        return new AccessTokenResponse(token.getToken());
    }
}
