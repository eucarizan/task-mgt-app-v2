package dev.nj.tms.task;

import jakarta.validation.constraints.NotBlank;

public record AssignTaskRequest(
        @NotBlank(message = "assignee is required")
        String assignee
) {
}
