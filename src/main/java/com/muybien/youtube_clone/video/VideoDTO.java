package com.muybien.youtube_clone.video;

import com.muybien.youtube_clone.comment.CommentDTO;
import lombok.Builder;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Builder
public record VideoDTO(
        String username,
        String title,
        String description,
        String videoUrl,
        String thumbnailUrl,
        AtomicInteger videoViewCounter,
        AtomicInteger likes,
        AtomicInteger disLikes,
        Set<CommentDTO> comments
) {
}
