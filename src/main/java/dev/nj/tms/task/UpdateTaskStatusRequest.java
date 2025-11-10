package dev.nj.tms.task;

import jakarta.validation.constraints.NotBlank;

public record UpdateTaskStatusRequest(
        @NotBlank(message = "status is required")
        String status
) {
}
