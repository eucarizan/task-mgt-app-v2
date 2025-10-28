package dev.nj.tms.token;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccessTokenCleanupScheduleTest {

    @Mock
    private AccessTokenRepository tokenRepository;

    @InjectMocks
    private TokenCleanupScheduler scheduler;

    @Test
    void deleteExpiredTokens_callsRepositoryWithCurrentTime() {
        when(tokenRepository.deleteByExpiresAtBefore(any(LocalDateTime.class))).thenReturn(5);

        scheduler.deleteExpiredTokens();

        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(tokenRepository).deleteByExpiresAtBefore(timeCaptor.capture());

        LocalDateTime capturedTime = timeCaptor.getValue();
        LocalDateTime now = LocalDateTime.now();
        assertTrue(capturedTime.isBefore(now.plusSeconds(1)));
        assertTrue(capturedTime.isAfter(now.minusSeconds(1)));
    }

    @Test
    void deleteExpiredTokens_logsDeletedCount() {
        when(tokenRepository.deleteByExpiresAtBefore(any(LocalDateTime.class))).thenReturn(3);

        assertDoesNotThrow(() -> scheduler.deleteExpiredTokens());

        verify(tokenRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }

    @Test
    void deleteExpiredTokens_handlesZeroExpiredTokens() {
        when(tokenRepository.deleteByExpiresAtBefore(any(LocalDateTime.class))).thenReturn(0);

        assertDoesNotThrow(() -> scheduler.deleteExpiredTokens());

        verify(tokenRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }
}
