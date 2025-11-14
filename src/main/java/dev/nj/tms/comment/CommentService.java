package dev.nj.tms.comment;

public interface CommentService {
    CommentResponse createComment(Long taskId, String text, String author);
}
