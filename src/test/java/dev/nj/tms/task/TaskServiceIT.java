package dev.nj.tms.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
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
public class TaskServiceIT {

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
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void it_noFilter_user1SeesThree() {
        String user1 = "user1@mail.com";
        String user2 = "user2@mail.com";

        Task task1 = new Task("Task 1 by User 1", "Description 1", user1);
        Task task2 = new Task("Task 2 by User 1", "Description 2", user1);
        Task task3 = new Task("Task 3 by User 2", "Description 3", user2);

        taskRepository.saveAll(List.of(task1, task2, task3));

        List<TaskResponse> tasks = taskService.getTasks();

        assertEquals(3, tasks.size(), "Should return 3 tasks");

        long countSelf = tasks.stream()
                .filter(task -> task.author().equalsIgnoreCase(user1))
                .count();
        long countOther = tasks.stream()
                .filter(task -> task.author().equalsIgnoreCase(user2))
                .count();

        assertEquals(2, countSelf);
        assertEquals(1, countOther);

        // verify order
        assertEquals("Task 1 by User 1", tasks.get(0).title());
        assertEquals("Task 2 by User 1", tasks.get(1).title());
        assertEquals("Task 3 by User 2", tasks.get(2).title());
    }

    @Test
    void it_filterSelf_user1SeesTwo() {
        Task task1 = new Task("T1", "D1", "user1@mail.com");
        Task task2 = new Task("T2", "D2", "user1@mail.com");
        Task task3 = new Task("T3", "D3", "user2@mail.com");

        taskRepository.saveAll(List.of(task1, task2, task3));

        List<TaskResponse> tasks = taskService.getTasksByAuthor("user1@mail.com");

        assertEquals(2, tasks.size());
        assertEquals(2, tasks.stream().filter(task -> "user1@mail.com".equalsIgnoreCase(task.author())).count());
    }

    @Test
    void it_filterOther_user1SeesOne() {
        Task task1 = new Task("T1", "D1", "user1@mail.com");
        Task task2 = new Task("T2", "D2", "user1@mail.com");
        Task task3 = new Task("T3", "D3", "user2@mail.com");

        taskRepository.saveAll(List.of(task1, task2, task3));

        List<TaskResponse> tasks = taskService.getTasksByAuthor("user2@mail.com");

        assertEquals(1, tasks.size());

        long user2TaskCount = tasks.stream().filter(task -> "user2@mail.com".equalsIgnoreCase(task.author())).count();

        assertEquals(1, user2TaskCount, "Should have exactly 1 task from user2");

        long otherUserTaskCount = tasks.stream().filter(task -> !"user2@mail.com".equalsIgnoreCase(task.author())).count();

        assertEquals(0, otherUserTaskCount, "Should have no tasks from other users");
    }

    @Test
    void it_filterUnknown_returnsEmpty() {
        List<TaskResponse> tasks = taskService.getTasksByAuthor("test@mail.com");

        assertEquals(0, tasks.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "not-an-email",
            "missingdomain@",
            "@missingusername",
            "invalid@domain",
            "spaces in@mail.com",
            "invalid@-domain.com",
            "test@domain.c",
            "test@domain..com",
            "test@.com"
    })
    @NullAndEmptySource
    void it_invalidAuthor_throws(String invalidAuthor) {
        assertThrows(IllegalArgumentException.class, () ->
                        taskService.getTasksByAuthor(invalidAuthor),
                "Should throw IllegalArgumentException for invalid author: " + invalidAuthor);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "first.last@example.com",
            "user123@example.co.uk",
            "user-name@example-domain.com",
            "user+tag@example.com",
            "user@sub.domain.com",
            "u@example.com",
            "user@example.info",
            "user@example.museum"
    })
    void it_validAuthor_doesNotThrow(String validAuthor) {
        taskService.getTasksByAuthor(validAuthor);
    }

    @Test
    void it_createTask_persistsAndReturnResponse() {
        String title = "Integration task";
        String description = "Persist me";
        String author = "it_user@example.com";

        TaskResponse response = taskService.createTask(title, description, author);

        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals(title, response.title());
        assertEquals(description, response.description());
        assertEquals("CREATED", response.status());
        assertEquals(author, response.author());
    }
}
