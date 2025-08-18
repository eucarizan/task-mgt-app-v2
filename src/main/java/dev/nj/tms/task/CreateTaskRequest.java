package dev.nj.tms.task;

import jakarta.validation.constraints.NotBlank;

public record CreateTaskRequest(
        @NotBlank
        String title,

        @NotBlank
        String description
) {
}
