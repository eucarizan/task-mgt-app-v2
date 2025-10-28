package dev.nj.tms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nj.tms.account.NewAccountDto;
import dev.nj.tms.token.AccessTokenResponse;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestUtils {
    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static String asJsonString(final Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    public static void register(String email, String password, MockMvc mockMvc) throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(new NewAccountDto(email, password))))
                .andExpect(status().isOk());
    }

    public static String createToken(String email, String password, MockMvc mockMvc) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/token")
                        .with(httpBasic(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        AccessTokenResponse tokenResponse = objectMapper.readValue(responseBody, AccessTokenResponse.class);
        return tokenResponse.token();
    }

}
