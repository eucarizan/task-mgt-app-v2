package dev.nj.tms.task;

public record TaskListResponse(
        String id,
        String title,
        String description,
        String status,
        String author,
        String assignee,
        int total_comments
) {
}
