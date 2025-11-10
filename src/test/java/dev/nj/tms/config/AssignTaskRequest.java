package dev.nj.tms.config;

import jakarta.validation.constraints.NotBlank;

public record AssignTaskRequest(
        @NotBlank(message = "assignee is required")
        String assignee
) {
}
