package dev.nj.tms.comment;

import dev.nj.tms.account.Account;
import dev.nj.tms.account.AccountRepository;
import dev.nj.tms.task.Task;
import dev.nj.tms.task.TaskRepository;
import dev.nj.tms.token.AccessToken;
import dev.nj.tms.token.AccessTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static dev.nj.tms.TestUtils.asJsonString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class CommentControllerIT {

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
    private CommentRepository commentRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccessTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder encoder;

    @BeforeEach
    void setup() {
        commentRepository.deleteAll();
        taskRepository.deleteAll();
        accountRepository.deleteAll();
        tokenRepository.deleteAll();
    }

    @Test
    void it_createComment_returns200_whenValidRequest() throws Exception {
        Account account = accountRepository.save(
                new Account("user@mail.com", encoder.encode("secureP1")));

        AccessToken token = tokenRepository.save(
                new AccessToken("test-token", account, LocalDateTime.now().plusHours(1)));

        Task task = taskRepository.save(
                new Task("Test task", "Description", "author@mail.com"));

        CreateCommentRequest request = new CreateCommentRequest("This is a comment");

        mockMvc.perform(post("/api/tasks/{taskId}/comments", task.getId())
                        .header("Authorization", "Bearer " + token.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk());
    }
}
