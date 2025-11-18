package dev.nj.tms.comment;

import java.util.List;

public interface CommentService {
    CommentResponse createComment(Long taskId, String text, String author);

    List<CommentResponse> getCommentsByTaskId(Long taskId);
}
