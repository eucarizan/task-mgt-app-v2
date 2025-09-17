package dev.nj.tms.task;

import dev.nj.tms.account.AccountRepository;
import dev.nj.tms.account.NewAccountDto;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void cleanDb() throws Exception {
        taskRepository.deleteAll();
        accountRepository.deleteAll();

        register("user1@mail.com", "secureP1");
        register("user2@mail.com", "secureP2");
        createTaskAs("user1@mail.com", "secureP1", "T1", "D1");
        createTaskAs("user1@mail.com", "secureP1", "T2", "D2");
        createTaskAs("user2@mail.com", "secureP2", "T3", "D3");
    }

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
        mockMvc.perform(get("/api/tasks")
                        .with(httpBasic("user1@mail.com", "secureP1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void it_filterSelf_user1SeesTwo() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .with(httpBasic("user1@mail.com", "secureP1"))
                        .param("author", "user1@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].author").value("user1@mail.com"));
    }

    @Test
    void it_filterOther_user1SeesOne() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .with(httpBasic("user1@mail.com", "secureP1"))
                        .param("author", "user2@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].author").value("user2@mail.com"));
    }

    private void register(String email, String password) throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(new NewAccountDto(email, password))))
                .andExpect(status().isOk());
    }

    private void createTaskAs(String email, String password, String title, String description) throws Exception{
        mockMvc.perform(post("/api/tasks")
                        .with(httpBasic(email, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(new CreateTaskRequest(title, description))))
                .andExpect(status().isOk());
    }
}
