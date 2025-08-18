package dev.nj.tms.task;

import dev.nj.tms.account.Account;
import dev.nj.tms.account.AccountRepository;
import dev.nj.tms.account.CustomUserDetailsService;
import dev.nj.tms.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static dev.nj.tms.TestUtils.asJsonString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import({SecurityConfig.class, CustomUserDetailsService.class})
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    PasswordEncoder passwordEncoder;

    @MockitoBean
    private TaskServiceImpl taskService;

    @MockitoBean
    private AccountRepository accountRepository;

    @Test
    @WithMockUser
    void getTasks_shouldReturn200WithMockUser() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk());
    }

    @Test
    void getTasks_shouldReturn200AndBody_whenValidBasicAuth() throws Exception {
        String email = "testuser@example.com";
        String password = "testpass123";

        Account mockAccount = new Account(email, passwordEncoder.encode(password));
        when(accountRepository.findByEmailIgnoreCase(email))
                .thenReturn(Optional.of(mockAccount));

        mockMvc.perform(get("/api/tasks")
                        .with(httpBasic(email, password)))
                .andExpect(status().isOk());
    }

    @Test
    void getTasks_shouldReturn401WhenNoAuth() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTasks_shouldReturn401WhenInvalidCredentials() throws Exception {
        String email = "testuser@example.com";
        String wrongPassword = "wrongpassword";

        mockMvc.perform(get("/api/tasks")
                        .with(httpBasic(email, wrongPassword)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createTask_shouldReturn200AndBody_whenValidRequestAndAuth() throws Exception {
        String title = "My Task";
        String description = "Do something important";
        String expectedAuthor = "user@example.com";

        CreateTaskRequest dto = new CreateTaskRequest(title, description);
        TaskResponse serviceResponse = new TaskResponse("42", title, description, "CREATED", expectedAuthor);

        when(taskService.createTask(eq(title), eq(description), eq(expectedAuthor))).thenReturn(serviceResponse);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.author").value("user@example.com"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createTask_shouldReturn400_whenTitleIsBlank() throws Exception {
        CreateTaskRequest dto = new CreateTaskRequest("   ", "Do something");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages", hasItem("title should not be blank")));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createTask_shouldReturn400_whenDescriptionIsBlank() throws Exception {
        CreateTaskRequest dto = new CreateTaskRequest("My Task", "   ");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages", hasItem("description should not be blank")));
    }

    @Test
    void createTask_shouldReturn400_whenNoAuth() throws Exception {
        CreateTaskRequest dto = new CreateTaskRequest("My Task", "Do something");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
