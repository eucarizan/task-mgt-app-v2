package dev.nj.tms.comment;

import dev.nj.tms.task.Task;
import dev.nj.tms.task.TaskNotFoundException;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class CommentServiceIT {

    private Task testTask;
    private static final String TEST_AUTHOR = "author@mail.com";
    private static final String TEST_COMMENTER = "commenter@mail.com";

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

        testTask = taskRepository.save(new Task("Test task", "Description", TEST_AUTHOR));
    }

    @Test
    void it_createComment_persistsAndReturnComment() {
        String text = "This is a test comment";

        CommentResponse response = commentService.createComment(testTask.getId(), text, TEST_COMMENTER);

        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals(testTask.getId().toString(), response.task_id());
        assertEquals(text, response.text());
        assertEquals(TEST_COMMENTER, response.author());

        Comment savedComment = commentRepository.findById(Long.parseLong(response.id())).orElseThrow();
        assertEquals(text, savedComment.getText());
        assertEquals(TEST_COMMENTER, savedComment.getAuthor());
    }

    @Test
    void it_createComment_taskNotFound_throwsTaskNotFoundException() {
        Long nonExistentTaskId = 999L;
        String text = "This is a comment";

        Exception exception = assertThrows(
                TaskNotFoundException.class,
                () -> commentService.createComment(nonExistentTaskId, text, TEST_COMMENTER));

        assertTrue(exception.getMessage().contains("Task not found"));
    }

    @Test
    void it_getCommentsById_returnsSortedNewestFirst() throws InterruptedException {
        Comment comment1 = new Comment(testTask.getId(), "First comment", "user1@mail.com");
        commentRepository.save(comment1);

        Thread.sleep(10);

        Comment comment2 = new Comment(testTask.getId(), "Second comment", "user2@mail.com");
        commentRepository.save(comment2);

        Thread.sleep(10);

        Comment comment3 = new Comment(testTask.getId(), "Third comment", "user3@mail.com");
        commentRepository.save(comment3);

        List<CommentResponse> comments = commentService.getCommentsByTaskId(testTask.getId());

        assertEquals(3, comments.size());
        assertEquals("Third comment", comments.get(0).text());
        assertEquals("Second comment", comments.get(1).text());
        assertEquals("First comment", comments.get(2).text());
    }
}
