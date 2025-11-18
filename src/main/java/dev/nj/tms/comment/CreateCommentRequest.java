package dev.nj.tms.comment;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
        @NotBlank(message = "text is required")
        String text
) {
}
