package com.muybien.youtube_clone.video;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record VideoUploadRequest(
        @NotNull(message = "Video file is required.")
        MultipartFile video,
        @NotNull(message = "Thumbnail file is required.")
        MultipartFile thumbnail,
        @NotNull(message = "Title is required.")
        @NotEmpty (message = "Title is required.")
        String title,
        @NotNull(message = "Description is required.")
        @NotEmpty(message = "Description is required.")
        String description
) {
}
