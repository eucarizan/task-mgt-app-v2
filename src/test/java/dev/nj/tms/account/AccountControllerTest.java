package dev.nj.tms.account;

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

        Account account = new Account(email, password);
        when(accountService.register(any(), any())).thenReturn(account);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        String invalidEmail = "invalid-email";
        String password = "secure123";

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + invalidEmail + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages").exists());
    }

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        String email = "user@email.com";
        String password = "secure123";

        when(accountService.register(any(), any()))
                .thenThrow(new EmailAlreadyExistsException("Email already exists: " + email));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn400WhenServiceThrowsIllegalArgumentException() throws Exception {
        String email = "user+tag@example.com";
        String password = "secure123";

        when(accountService.register(any(), any()))
                .thenThrow(new IllegalArgumentException("Invalid email format"));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn400WithValidationErrorsWhenDtoValidationFails() throws Exception {
        String invalidEmail = "not-an-email";
        String shortPassword = "123";

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + invalidEmail + "\",\"password\":\"" + shortPassword + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages").value(hasSize(2)))
                .andExpect(jsonPath("$.messages").value(hasItem("Incorrect email format")))
                .andExpect(jsonPath("$.messages").value(hasItem("Password should be at least 6 characters")));
    }

    @Test
    void shouldReturn400WhenServiceThrowsValidationFailsForNullEmail() throws Exception {
        String password = "secure123";

        when(accountService.register(any(), any()))
                .thenThrow(new IllegalArgumentException("Email is required"));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":null,\"password\":\"" + password + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages").value(hasItem("Email should not be blank")));
    }
}
