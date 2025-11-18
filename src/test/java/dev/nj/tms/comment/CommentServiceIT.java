package dev.nj.tms.comment;

import dev.nj.tms.task.Task;
import dev.nj.tms.task.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class CommentServiceIT {

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
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setup() {
        commentRepository.deleteAll();
        taskRepository.deleteAll();
    }

    @Test
    void it_createComment_persistsAndReturnComment() {
        Task task = taskRepository.save(new Task("Test task", "Description", "author@mail.com"));
        String text = "This is a test comment";
        String authorEmail = "commenter@mail.com";

        CommentResponse response = commentService.createComment(task.getId(), text, authorEmail);

        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals(task.getId().toString(), response.task_id());
        assertEquals(text, response.text());
        assertEquals(authorEmail, response.author());

        Comment savedComment = commentRepository.findById(Long.parseLong(response.id())).orElseThrow();
        assertEquals(text, savedComment.getText());
        assertEquals(authorEmail, savedComment.getAuthor());
    }
}
