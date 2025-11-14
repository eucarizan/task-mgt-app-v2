package dev.nj.tms.comment;

import org.springframework.stereotype.Component;

@Component
public class CommentMapper {
    public CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId().toString(),
                comment.getTaskId().toString(),
                comment.getText(),
                comment.getAuthor()
        );
    }
}
