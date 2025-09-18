package dev.nj.tms.task;

import dev.nj.tms.account.AccountRepository;
import dev.nj.tms.account.NewAccountDto;
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

import static dev.nj.tms.TestUtils.asJsonString;
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
public class TaskControllerIntegrationTest {

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

    @Test
    void it_authWithRegisteredUser_returns200() throws Exception {
        String email = "testuser@example.com";
        String password = "testpass123";

        NewAccountDto dto = new NewAccountDto(email, password);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks")
                        .with(httpBasic(email, password)))
                .andExpect(status().isOk());
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
    void it_noFilter_user1SeesThree() throws Exception {
        setupTestData();
        mockMvc.perform(get("/api/tasks")
                        .with(httpBasic("user1@mail.com", "secureP1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void it_filterSelf_user1SeesTwo() throws Exception {
        setupTestData();
        mockMvc.perform(get("/api/tasks")
                        .with(httpBasic("user1@mail.com", "secureP1"))
                        .param("author", "user1@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].author").value("user1@mail.com"));
    }

    @Test
    void it_filterOther_user1SeesOne() throws Exception {
        setupTestData();
        mockMvc.perform(get("/api/tasks")
                        .with(httpBasic("user1@mail.com", "secureP1"))
                        .param("author", "user2@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].author").value("user2@mail.com"));
    }

    @Test
    void it_filterUnknown_returnsEmpty() throws Exception {
        setupTestData();
        mockMvc.perform(get("/api/tasks")
                        .with(httpBasic("user1@mail.com", "secureP1"))
                        .param("author", "test@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void it_invalidAuthor_returns400() throws Exception {
        setupTestData();
        mockMvc.perform(get("/api/tasks")
                        .with(httpBasic("user1@mail.com", "secureP1"))
                        .param("author", "not-an-email"))
                .andExpect(status().isBadRequest());
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

        CreateTaskRequest dto = new CreateTaskRequest("My Task", "Do something important");
        mockMvc.perform(post("/api/tasks")
                        .with(httpBasic(email, password))
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

        CreateTaskRequest dto = new CreateTaskRequest("   ", "Do something");
        mockMvc.perform(post("/api/tasks")
                        .with(httpBasic(email, password))
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

        CreateTaskRequest dto = new CreateTaskRequest("My Task", "   ");
        mockMvc.perform(post("/api/tasks")
                        .with(httpBasic(email, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages", hasItem("description should not be blank")));
    }

    private void register(String email, String password) throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(new NewAccountDto(email, password))))
                .andExpect(status().isOk());
    }

    private void setupTestData() throws Exception {
        taskRepository.deleteAll();
        accountRepository.deleteAll();

        register("user1@mail.com", "secureP1");
        register("user2@mail.com", "secureP2");
        createTaskAs("user1@mail.com", "secureP1", "T1", "D1");
        createTaskAs("user1@mail.com", "secureP1", "T2", "D2");
        createTaskAs("user2@mail.com", "secureP2", "T3", "D3");
    }

    private void createTaskAs(String email, String password, String title, String description) throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .with(httpBasic(email, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(new CreateTaskRequest(title, description))))
                .andExpect(status().isOk());
    }
}
