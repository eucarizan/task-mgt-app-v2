package dev.nj.tms.comment;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Locale;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<Void> createComment(@PathVariable Long taskId,
                                              @Valid @RequestBody CreateCommentRequest request,
                                              Principal principal) {
        String author = principal.getName().toLowerCase(Locale.ROOT);
        logger.info("Received request to create comment on task {} by {}", taskId, author);
        commentService.createComment(taskId, request.text(), author);
        logger.info("Successfully created comment on task {}", taskId);
        return ResponseEntity.ok().build();
    }
}
