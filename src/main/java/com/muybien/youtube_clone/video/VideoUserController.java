package com.muybien.youtube_clone.video;

import com.muybien.youtube_clone.comment.CommentRequest;
import com.muybien.youtube_clone.comment.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoUserController {

    private final VideoService videoService;
    private final CommentService commentService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(CREATED)
    public VideoUploadResponse uploadVideo(
            @Valid VideoUploadRequest request,
            Authentication connectedUser
    ) {
        return videoService.uploadVideo(request.video(), request.thumbnail(),
                request.title(), request.description(), connectedUser);
    }

    @DeleteMapping("{videoId}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVideo(
            @PathVariable Integer videoId,
            Authentication connectedUser
    ) {
        videoService.deleteVideo(videoId, connectedUser);
    }

    @PostMapping("{videoId}/like")
    @ResponseStatus(OK)
    public VideoDTO likeVideo(
            @PathVariable Integer videoId,
            Authentication connectedUser
    ) {
        return videoService.incrementVideoLike(videoId, connectedUser);
    }

    @PostMapping("{videoId}/dislike")
    @ResponseStatus(OK)
    public VideoDTO disLikeVideo(
            @PathVariable Integer videoId,
            Authentication connectedUser
    ) {
        return videoService.incrementVideoDisLike(videoId, connectedUser);
    }

    @PostMapping({"{videoId}/comment"})
    @ResponseStatus(OK)
    public void addComment(
            @PathVariable Integer videoId,
            @RequestParam("comment") CommentRequest commentInput,
            Authentication connectedUser
    ) {
        commentService.addComment(videoId, commentInput, connectedUser);
    }
}
