package dev.nj.tms.config;

import jakarta.validation.constraints.NotBlank;

public record UpdateTaskStatusRequest(
        @NotBlank(message = "status is required")
        String status
) {
}
