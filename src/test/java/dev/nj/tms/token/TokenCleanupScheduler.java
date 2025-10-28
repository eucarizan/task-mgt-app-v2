package dev.nj.tms.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TokenCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TokenCleanupScheduler.class);
    private final AccessTokenRepository tokenRepository;

    public TokenCleanupScheduler(AccessTokenRepository accessTokenRepository) {
        this.tokenRepository = accessTokenRepository;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void deleteExpiredTokens() {
        logger.info("Running expired token cleanup job");
        int deletedCount = tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        logger.info("Deleted {} expired tokens", deletedCount);
    }
}
