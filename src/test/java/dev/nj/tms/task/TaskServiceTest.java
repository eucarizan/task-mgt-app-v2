package dev.nj.tms.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    void getTasks_returnsVisibleTasks_whenNoAuthFilter() {
        String currentUserEmail = "user1@example.com";

        Task t1 = new Task("T1", "D1", "user1@mail.com");
        Task t2 = new Task("T2", "D2", "user1@mail.com");
        Task t3 = new Task("T3", "D3", "user2@mail.com");

        when(taskRepository.findAll()).thenReturn(List.of(t1, t2, t3));
        when(taskMapper.toResponse(t1)).thenReturn(new TaskResponse("1", "T1", "D1", "CREATED", "user1@mail.com"));
        when(taskMapper.toResponse(t2)).thenReturn(new TaskResponse("2", "T2", "D2", "CREATED", "user1@mail.com"));
        when(taskMapper.toResponse(t3)).thenReturn(new TaskResponse("3", "T3", "D3", "CREATED", "user2@mail.com"));

        var responses = taskService.getTasks();

        assertEquals(3, responses.size());
        long user1Count = responses.stream().filter(r -> "user1@mail.com".equals(r.author())).count();
        long user2Count = responses.stream().filter(r -> "user2@mail.com".equals(r.author())).count();
        assertEquals(2, user1Count);
        assertEquals(1, user2Count);
        verify(taskRepository).findAll();
        verify(taskMapper, times(3)).toResponse(any(Task.class));
    }

    @Test
    void getTasks_filterBySelf_whenAuthorIsCurrentUser() {
        Task t1 = new Task("T1", "D1", "user1@mail.com");
        Task t2 = new Task("T2", "D2", "user1@mail.com");

        when(taskRepository.findAllByAuthorIgnoreCase(any(String.class), any(Sort.class))).thenReturn(List.of(t1, t2));
        when(taskMapper.toResponse(t1)).thenReturn(new TaskResponse("1", "T1", "D1", "CREATED", "user1@mail.com"));
        when(taskMapper.toResponse(t2)).thenReturn(new TaskResponse("2", "T2", "D2", "CREATED", "user1@mail.com"));

        var responses = taskService.getTasksByAuthor("user1@mail.com");

        assertEquals(2, responses.size());
        long user1Count = responses.stream().filter(r -> "user1@mail.com".equals(r.author())).count();
        assertEquals(2, user1Count);
        verify(taskRepository).findAllByAuthorIgnoreCase(any(String.class), any(Sort.class));
        verify(taskMapper, times(2)).toResponse(any(Task.class));
    }

    @Test
    void createTask_shouldReturnStatusCreatedAndLowercasedAuthor() {
        String title = "My Task";
        String description = "Do something important";
        String authorEmail = "User@Example.com";
        String expectedAuthor = "user@example.com";

        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        when(taskMapper.toResponse(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            return new TaskResponse("1", t.getTitle(), t.getDescription(), t.getStatus().toString(), t.getAuthor());
        });

        TaskResponse response = taskService.createTask(title, description, authorEmail);

        assertNotNull(response);
        assertEquals("1", response.id());
        assertNotNull(response.id());
        assertEquals(title, response.title());
        assertEquals(description, response.description());
        assertEquals("CREATED", response.status());
        assertEquals(expectedAuthor, response.author());

        ArgumentCaptor<Task> savedTaskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(savedTaskCaptor.capture());
        assertEquals(expectedAuthor, savedTaskCaptor.getValue().getAuthor());
    }
}
