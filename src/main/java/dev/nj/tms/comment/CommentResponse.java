package dev.nj.tms.comment;

public record CommentResponse(
        String id,
        String task_id,
        String text,
        String author
) {
}
