package dev.nj.tms.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TaskServiceTest {

    @Test
    void createTask_shouldReturnStatusCreatedAndLowercasedAuthor() {
        TaskRepository taskRepository = mock(TaskRepository.class);

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task toSave = invocation.getArgument(0);

            try {
                var idField = Task.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(toSave, 1L);
            } catch (Exception ignored) {}
            return toSave;
        });

        TaskServiceImpl taskService = new TaskServiceImpl(taskRepository);

        String title = "My Task";
        String description = "Do something important";
        String authorEmail = "User@Example.com";

        TaskResponse response = taskService.createTask(title, description, authorEmail);

        assertNotNull(response.id());
        assertEquals(title, response.title());
        assertEquals(description, response.description());
        assertEquals("CREATED", response.status());
        assertEquals("user@example.com", response.author());
        verify(taskRepository).save(any(Task.class));
    }
}
