package com.muybien.youtube_clone.video;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoGuestController {

    private final VideoService videoService;

    @GetMapping("{videoId}")
    @ResponseStatus(OK)
    public VideoDTO getVideoDetails(@PathVariable Integer videoId, Authentication connectedUser) {
        return videoService.getVideoDetails(videoId, connectedUser);
    }

    @GetMapping
    @ResponseStatus(OK)
    public List<VideoDTO> findAllVideos() {
        return videoService.findAllVideos();
    }
}
