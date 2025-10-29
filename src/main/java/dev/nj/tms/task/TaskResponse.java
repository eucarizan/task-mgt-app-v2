package dev.nj.tms.task;

public record TaskResponse(
        String id,
        String title,
        String description,
        String status,
        String author,
        String assignee
) {
}
