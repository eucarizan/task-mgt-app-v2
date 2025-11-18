package dev.nj.tms.token;

import dev.nj.tms.account.Account;
import dev.nj.tms.account.AccountRepository;
import dev.nj.tms.account.CustomUserDetailsService;
import dev.nj.tms.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccessTokenController.class)
@Import({TestSecurityConfig.class, CustomUserDetailsService.class})
public class AccessTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void createToken_validBasic_returnsToken() throws Exception {
        String email = "user1@mail.com";
        String password = "secureP1";
        String encoded = passwordEncoder.encode(password);
        Account mockAccount = new Account(email, encoded);

        when(accountRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(mockAccount));
        when(passwordEncoder.matches(password, encoded)).thenReturn(true);

        AccessTokenResponse tokenResponse = new AccessTokenResponse("jwt-token");
        when(accessTokenService.createToken(eq(email))).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/auth/token")
                        .with(httpBasic(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void createToken_wrongPassword_returns401() throws Exception {
        String email = "user1@mail.com";
        String password = "secureP1";
        String wrongPassword = "wrongP1";
        String encoded = passwordEncoder.encode(password);
        Account mockAccount = new Account(email, encoded);

        when(accountRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(mockAccount));
        when(passwordEncoder.matches(wrongPassword, encoded)).thenReturn(false);

        mockMvc.perform(post("/api/auth/token")
                        .with(httpBasic(email, wrongPassword)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createToken_emptyCredentials_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/token")
                        .with(httpBasic("", "")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createToken_invalidAuth_doesNotCallService() throws Exception {
        String email = "nonexistent@mail.com";

        when(accountRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/token")
                        .with(httpBasic(email, "password")))
                .andExpect(status().isUnauthorized());

        verify(accessTokenService, never()).createToken(any());
    }

    @Test
    void createToken_missingAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createToken_malformedToken_returns401() throws Exception {
        String fakeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        mockMvc.perform(post("/api/auth/token")
                        .header("Authorization", "Basic " + fakeToken))
                .andExpect(status().isUnauthorized());
    }
}
