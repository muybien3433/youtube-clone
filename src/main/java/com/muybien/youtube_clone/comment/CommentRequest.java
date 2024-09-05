package com.muybien.youtube_clone.comment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CommentRequest(
        @NotEmpty(message = "Text field can't be empty.")
        @NotNull(message = "Text field can't be empty.")
        String commentInput
) {
}
