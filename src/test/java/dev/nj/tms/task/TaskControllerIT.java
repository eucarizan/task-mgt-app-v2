package dev.nj.tms.task;

import dev.nj.tms.account.AccountRepository;
import dev.nj.tms.account.NewAccountDto;
import dev.nj.tms.token.AccessTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static dev.nj.tms.TestUtils.*;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@AutoConfigureMockMvc
public class TaskControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("tms_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccessTokenRepository tokenRepository;

    private String user1Token;

    @Test
    void it_authWithRegisteredUser_returns200() throws Exception {
        String email = "testuser@example.com";
        String password = "testpass123";

        NewAccountDto dto = new NewAccountDto(email, password);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isOk());

        String token = getTokenForUser(email, password);

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private String getTokenForUser(String email, String password) throws Exception {
        return createToken(email, password, mockMvc);
    }

    @Test
    void it_authNonExistingUser_returns401() throws Exception {
        String email = "nonexistent@example.com";
        String password = "wrongpassword";

        mockMvc.perform(get("/api/tasks")
                        .with(httpBasic(email, password)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void it_authWrongPassword_returns401() throws Exception {
        String email = "testuser2@example.com";
        String password = "correctpassword123";

        NewAccountDto dto = new NewAccountDto(email, password);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isOk());

        String wrongPassword = "wrongpassword123";

        mockMvc.perform(get("/api/tasks")
                        .with(httpBasic(email, wrongPassword)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void it_getTasks_noFilter_returnsAll() throws Exception {
        setupTestData();
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void it_getTasks_filterSelf_returnsOwnTasks() throws Exception {
        setupTestData();
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + user1Token)
                        .param("author", "user1@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].author").value("user1@mail.com"));
    }

    @Test
    void it_getTasks_filterOther_returnsFilteredTasks() throws Exception {
        setupTestData();
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + user1Token)
                        .param("author", "user2@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].author").value("user2@mail.com"));
    }

    @Test
    void it_getTasks_filterUnknown_returnsEmpty() throws Exception {
        setupTestData();
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + user1Token)
                        .param("author", "test@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void it_getTasks_invalidAuthor_returns400() throws Exception {
        setupTestData();
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + user1Token)
                        .param("author", "not-an-email"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void it_getTasks_sortedByCreatedDesc() throws Exception {
        setupTestData();
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].title").value("T3"))
                .andExpect(jsonPath("$[1].title").value("T2"))
                .andExpect(jsonPath("$[2].title").value("T1"));
    }

    @Test
    void it_getTasksByAuthor_sortedByCreatedDesc() throws Exception {
        setupTestData();
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + user1Token)
                        .param("author", "user1@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("T2"))
                .andExpect(jsonPath("$[1].title").value("T1"));
    }

    @Test
    void it_createTask_returns200AndBody_whenValidRequestAndAuth() throws Exception {
        String email = "it_create_user@mail.com";
        String password = "it_pass_123";
        NewAccountDto acc = new NewAccountDto(email, password);
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(acc)))
                .andExpect(status().isOk());

        String token = createToken(email, password, mockMvc);

        CreateTaskRequest dto = new CreateTaskRequest("My Task", "Do something important");
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("My Task"))
                .andExpect(jsonPath("$.description").value("Do something important"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.author").value(email));
    }

    @Test
    void it_createTask_returns400_whenTitlesBlank() throws Exception {
        String email = "it_blank_title@mail.com";
        String password = "p@ssw0rd";
        NewAccountDto acc = new NewAccountDto(email, password);
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(acc)))
                .andExpect(status().isOk());

        String token = createToken(email, password, mockMvc);

        CreateTaskRequest dto = new CreateTaskRequest("   ", "Do something");
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages", hasItem("title should not be blank")));
    }

    @Test
    void it_createTask_returns400_whenDescriptionIsBlank() throws Exception {
        String email = "it_blank_desc@mail.com";
        String password = "p@ssw0rd";
        NewAccountDto acc = new NewAccountDto(email, password);
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(acc)))
                .andExpect(status().isOk());

        String token = createToken(email, password, mockMvc);

        CreateTaskRequest dto = new CreateTaskRequest("My Task", "   ");
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages", hasItem("description should not be blank")));
    }

    @Test
    void it_createTask_returns401_whenNoAuth() throws Exception {
        CreateTaskRequest dto = new CreateTaskRequest("My Task", "Do something");
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isUnauthorized());
    }

    private void setupTestData() throws Exception {
        taskRepository.deleteAll();
        tokenRepository.deleteAll();
        accountRepository.deleteAll();

        register("user1@mail.com", "secureP1", mockMvc);
        register("user2@mail.com", "secureP2", mockMvc);

        user1Token = createToken("user1@mail.com", "secureP1", mockMvc);
        String user2Token = createToken("user2@mail.com", "secureP2", mockMvc);

        createTaskWithToken(user1Token, "T1", "D1");
        createTaskWithToken(user1Token, "T2", "D2");
        createTaskWithToken(user2Token, "T3", "D3");
    }

    private void createTaskWithToken(String token, String title, String description) throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(new CreateTaskRequest(title, description))))
                .andExpect(status().isOk());
    }

}
