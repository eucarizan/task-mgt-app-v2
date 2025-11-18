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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class CommentControllerIT {

    private static final String COMMENTS_URL = "/api/tasks/{taskId}/comments";

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

    private AccessToken testToken;
    private Task testTask;

    @BeforeEach
    void setup() {
        commentRepository.deleteAll();
        tokenRepository.deleteAll();
        taskRepository.deleteAll();
        accountRepository.deleteAll();

        Account testAccount = accountRepository.save(
                new Account("user@mail.com", encoder.encode("secureP1")));

        testToken = tokenRepository.save(
                new AccessToken("test-token", testAccount, LocalDateTime.now().plusHours(1)));

        testTask = taskRepository.save(
                new Task("Test task", "Description", "author@mail.com"));
    }

    @Test
    void it_createComment_returns200_whenValidRequest() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("This is a comment");

        mockMvc.perform(post(COMMENTS_URL, testTask.getId())
                        .header("Authorization", "Bearer " + testToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void it_createComment_blankText_returns400() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("   ");

        mockMvc.perform(post(COMMENTS_URL, testTask.getId())
                        .header("Authorization", "Bearer " + testToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void it_createComment_taskNotFound_returns404() throws Exception {
        Long nonExistentTaskId = 999L;
        CreateCommentRequest request = new CreateCommentRequest("This is a comment");

        mockMvc.perform(post(COMMENTS_URL, nonExistentTaskId)
                        .header("Authorization", "Bearer " + testToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void it_createComment_noAuth_returns401() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("This is a comment");

        mockMvc.perform(post(COMMENTS_URL, testTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void it_getComments_returns200WithSortedComments() throws Exception {
        Comment comment1 = commentRepository.save(
                new Comment(testTask.getId(), "First comment", "user1@mail.com"));
        Thread.sleep(10);
        Comment comment2 = commentRepository.save(
                new Comment(testTask.getId(), "Second comment", "user2@mail.com"));

        mockMvc.perform(get(COMMENTS_URL, testTask.getId())
                        .header("Authorization", "Bearer " + testToken.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].text").value("Second comment"))
                .andExpect(jsonPath("$[1].text").value("First comment"));
    }
}
