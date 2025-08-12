package dev.nj.tms.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturn200WhenCreatingValidAccount() throws Exception {
        String email = "user@email.com";
        String password = "secure123";

        NewAccountDto dto = new NewAccountDto(email, password);
        Account account = new Account(email, password);
        when(accountService.register(any(), any())).thenReturn(account);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        NewAccountDto dto = new NewAccountDto("invalid-email", "secure123");

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages").exists());
    }

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        NewAccountDto dto = new NewAccountDto("user@email.com", "secure123");

        when(accountService.register(any(), any()))
                .thenThrow(new EmailAlreadyExistsException("Email already exists: " + dto.email()));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn400WhenServiceThrowsIllegalArgumentException() throws Exception {
        NewAccountDto dto = new NewAccountDto("user+tag@example.com", "secure123");

        when(accountService.register(any(), any()))
                .thenThrow(new IllegalArgumentException("Invalid email format"));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn400WithValidationErrorsWhenDtoValidationFails() throws Exception {
        NewAccountDto dto = new NewAccountDto("not-an-email", "123");

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages").value(hasSize(2)))
                .andExpect(jsonPath("$.messages").value(hasItem("Incorrect email format")))
                .andExpect(jsonPath("$.messages").value(hasItem("Password should be at least 6 characters")));
    }

    @Test
    void shouldReturn400WhenServiceValidationFailsForNullEmail() throws Exception {
        NewAccountDto dto = new NewAccountDto("user+tag@example.com", "secure123");

        when(accountService.register(any(), any()))
                .thenThrow(new IllegalArgumentException("Invalid email format"));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid email format"));
    }

    @Test
    void shouldReturn400WhenDtoValidationFailsForNullEmail() throws Exception {
        NewAccountDto dto = new NewAccountDto(null, "secure123");

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages").value(hasItem("Email should not be blank")));
    }

    @Test
    void shouldReturn500WhenDatabaseOperationFails() throws Exception {
        NewAccountDto dto = new NewAccountDto("user@example.com", "secure123");

        when(accountService.register(any(), any()))
                .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isInternalServerError());
    }

    private static String asJsonString(final Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }
}
