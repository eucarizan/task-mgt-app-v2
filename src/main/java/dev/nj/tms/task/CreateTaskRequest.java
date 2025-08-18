package dev.nj.tms.task;

import jakarta.validation.constraints.NotBlank;

public record CreateTaskRequest(
        @NotBlank(message = "title should not be blank")
        String title,

        @NotBlank(message = "description should not be blank")
        String description
) {
}
