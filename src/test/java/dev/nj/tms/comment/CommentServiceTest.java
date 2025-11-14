package dev.nj.tms.comment;

import dev.nj.tms.task.Task;
import dev.nj.tms.task.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void createComment_validText_returnsComment() {
        Long taskId = 1L;
        String text = "Great task!";
        String author = "user1@mail.com";

        Task task = new Task("Task", "Description", "author@mail.com");
        Comment comment = new Comment(taskId, text, author);
        CommentResponse expectedResponse = new CommentResponse("1", "1", text, author);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toResponse(comment)).thenReturn(expectedResponse);

        CommentResponse response = commentService.createComment(taskId, text, author);

        assertEquals(text, response.text());
        assertEquals(author, response.author());
        verify(taskRepository).findById(taskId);
        verify(commentRepository).save(any(Comment.class));
    }

}
