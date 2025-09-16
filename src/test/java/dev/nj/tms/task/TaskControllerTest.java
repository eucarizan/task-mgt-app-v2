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

import java.util.List;
import java.util.Optional;

import static dev.nj.tms.TestUtils.asJsonString;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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

    @Test
    @WithMockUser(username = "user1@mail.com")
    void get_noFilter_returnsThree_forUser1() throws Exception {
        List<TaskResponse> expectedTasks = List.of(
                new TaskResponse("1", "T1", "D1", "CREATED", "user1@mail.com"),
                new TaskResponse("2", "T2", "D2", "CREATED", "user1@mail.com"),
                new TaskResponse("3", "T3", "D3", "CREATED", "user2@mail.com")
        );

        when(taskService.getTasks()).thenReturn(expectedTasks);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        verify(taskService).getTasks();
    }

    @Test
    @WithMockUser(username = "user1@mail.com")
    void get_filterSelf_returnsTwo() throws Exception {
        List<TaskResponse> expectedTasks = List.of(
                new TaskResponse("1", "T1", "D1", "CREATED", "user1@mail.com"),
                new TaskResponse("2", "T2", "D2", "CREATED", "user1@mail.com")
        );

        when(taskService.getTasksByAuthor("user1@mail.com")).thenReturn(expectedTasks);

        mockMvc.perform(get("/api/tasks")
                        .param("author", "user1@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[1].id").value("2"));

        verify(taskService).getTasksByAuthor("user1@mail.com");
    }

    @Test
    @WithMockUser(username = "user1@mail.com")
    void get_filterOther_returnsOne() throws Exception {
        List<TaskResponse> expectedTasks = List.of(
                new TaskResponse("1", "T1", "D1", "CREATED", "user2@mail.com")
        );

        when(taskService.getTasksByAuthor("user2@mail.com")).thenReturn(expectedTasks);

        mockMvc.perform(get("/api/tasks")
                        .param("author", "user2@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(taskService).getTasksByAuthor("user2@mail.com");
    }

    @Test
    @WithMockUser(username = "user1@mail.com")
    void get_filterUnknown_returnsEmpty() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .param("author", "unknown@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(taskService).getTasksByAuthor("unknown@mail.com");
    }

    @Test
    @WithMockUser(username = "user1@mail.com")
    void get_invalidAuthor_returns400() throws Exception {
        when(taskService.getTasksByAuthor("not-an-email"))
                .thenThrow(new IllegalArgumentException("Author must be in valid format"));

        mockMvc.perform(get("/api/tasks")
                        .param("author", "not-an-email"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(IllegalArgumentException.class, result.getResolvedException()));

        verify(taskService).getTasksByAuthor("not-an-email");
    }
}
