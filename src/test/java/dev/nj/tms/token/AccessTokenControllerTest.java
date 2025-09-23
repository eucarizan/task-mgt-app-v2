package dev.nj.tms.token;

import dev.nj.tms.account.CustomUserDetailsService;
import dev.nj.tms.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccessTokenController.class)
@Import({SecurityConfig.class})
public class AccessTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @Test
    @WithMockUser(username = "user1@mail.com", password = "secureP1")
    void createToken_validBasic_returnsToken() throws Exception {
        String email = "user1@mail.com";
        String password = "secureP1";
        AccessTokenResponse tokenResponse = new AccessTokenResponse("jwt-token");

        when(accessTokenService.createToken(eq(email), eq(password))).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/auth/token")
                        .with(httpBasic(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }
}
