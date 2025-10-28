package dev.nj.tms.token;

import dev.nj.tms.account.AccountRepository;
import dev.nj.tms.task.TaskRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static dev.nj.tms.TestUtils.objectMapper;
import static dev.nj.tms.TestUtils.register;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@AutoConfigureMockMvc
public class AccessTokenControllerIT {

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
    private TaskRepository taskRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccessTokenRepository tokenRepository;

    @BeforeEach
    void setup() {
        taskRepository.deleteAll();
        tokenRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void it_getTasks_withValidToken_returns200() throws Exception {
        String email = "tokenuser@mail.com";
        String password = "secureP1";

        register(email, password, mockMvc);
        String token = createToken(email, password);

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void it_getTasks_withInvalidToken_returns401() throws Exception {
        String fakeToken = "invalid-token-12345";

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + fakeToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication failed"))
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    @Test
    void it_getTasks_withNoToken_returns401() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void it_createTask_withValidToken_returns200() throws Exception {
        String email = "taskcreator@email.com";
        String password = "secureP2";

        register(email, password, mockMvc);
        String token = createToken(email, password);

        String taskJson = """
                {
                    "title": "Token Task",
                    "description": "Created with bearer token"
                }
                """;

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Token Task"))
                .andExpect(jsonPath("$.author").value(email));
    }

    @Test
    void it_createTask_withInvalidToken_returns401() throws Exception {
        String taskJson = """
                {
                    "title": "Test Task",
                    "description": "Test Description"
                }
                """;

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication failed"))
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    @Test
    void it_createTask_withMissingBearerPrefix_returns401() throws Exception {
        String email = "user@mail.com";
        String password = "secureP1";

        register(email, password, mockMvc);
        String token = createToken(email, password);

        String taskJson = """
                {
                    "title": "Test Task",
                    "description": "Test Description"
                }
                """;

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication failed"))
                .andExpect(jsonPath("$.message").value("Malformed authorization header. Expected format: 'Bearer <token>'"));
    }

    private String createToken(String email, String password) throws Exception {
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
