package com.muybien.youtube_clone.comment;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CommentDTO(
        String authorName,
        String commentInput,
        LocalDateTime createdDate) {
}
