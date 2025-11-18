package dev.nj.tms.comment;

import dev.nj.tms.account.AccountRepository;
import dev.nj.tms.account.CustomUserDetailsService;
import dev.nj.tms.config.TestSecurityConfig;
import dev.nj.tms.task.TaskNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static dev.nj.tms.TestUtils.asJsonString;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@Import({TestSecurityConfig.class, CustomUserDetailsService.class})
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private AccountRepository accountRepository;

    @Test
    @WithMockUser(username = "user@mail.com")
    void createComment_validRequest_returns200() throws Exception {
        Long taskId = 1L;
        String text = "Great task!";
        String author = "user@mail.com";
        CreateCommentRequest request = new CreateCommentRequest(text);

        CommentResponse response = new CommentResponse("1", "1", text, author);

        when(commentService.createComment(taskId, text, author)).thenReturn(response);

        mockMvc.perform(post("/api/tasks/{taskId}/comments", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk());

        verify(commentService).createComment(taskId, text, author);
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    void createComment_blankText_returns400() throws Exception {
        Long taskId = 1L;
        CreateCommentRequest request = new CreateCommentRequest("   ");

        mockMvc.perform(post("/api/tasks/{taskId}/comments", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages", hasItem("text is required")));
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    void createComment_taskNotFound_returns404() throws Exception {
        Long taskId = 999L;
        String text = "Comment on missing task";
        String author = "user@mail.com";
        CreateCommentRequest request = new CreateCommentRequest(text);

        when(commentService.createComment(taskId, text, author))
                .thenThrow(new TaskNotFoundException("Task not found wit id: 999"));

        mockMvc.perform(post("/api/tasks/{taskId}/comments", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(TaskNotFoundException.class, result.getResolvedException()));
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    void getComments_validTaskId_returns200WithComments() throws Exception {
        Long taskId = 1L;
        List<CommentResponse> comments = List.of(
                new CommentResponse("2", "1", "Second comment", "user2@mail.com"),
                new CommentResponse("1", "1", "First comment", "user1@mail.com")
        );

        when(commentService.getCommentsByTaskId(taskId)).thenReturn(comments);

        mockMvc.perform(get("/api/tasks/{taskId}/comments", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("2"))
                .andExpect(jsonPath("$[0].text").value("Second comment"))
                .andExpect(jsonPath("$[1].id").value("1"))
                .andExpect(jsonPath("$[1].text").value("First comment"));

        verify(commentService).getCommentsByTaskId(taskId);
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    void getComments_taskNotFound_returns404() throws Exception {
        Long taskId = 999L;

        when(commentService.getCommentsByTaskId(taskId))
                .thenThrow(new TaskNotFoundException("Task not found with id: 999"));

        mockMvc.perform(get("/api/tasks/{taskId}/comments", taskId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(TaskNotFoundException.class, result.getResolvedException()));
    }
}
