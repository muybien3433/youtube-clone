package com.muybien.youtube_clone.video;

import com.muybien.youtube_clone.comment.Comment;
import com.muybien.youtube_clone.config.JwtService;
import com.muybien.youtube_clone.token.TokenRepository;
import com.muybien.youtube_clone.user.User;
import com.muybien.youtube_clone.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VideoGuestController.class)
public class VideoGuestControllerTest {

    @MockBean private VideoService videoService;
    @MockBean private UserService userService;
    @MockBean private VideoDTOMapper videoDTOMapper;
    @MockBean private JwtService jwtService;
    @MockBean private TokenRepository tokenRepository;
    @Autowired private MockMvc mockMvc;

    private final int videoId = 1;
    private VideoDTO videoDTO;

    @BeforeEach
    public void setUp() {
        User user = User.builder()
                .firstname("Joe")
                .lastname("Smith")
                .build();

        Comment comment = Comment.builder()
                .authorName(user.getFullName())
                .commentInput("Sample comment")
                .createdDate(LocalDateTime.now())
                .build();

        Video video = Video.builder()
                .user(user)
                .videoUrl("http://video-url.com")
                .thumbnailUrl("http://thumbnail-url.com")
                .title("Sample video")
                .description("Sample description")
                .likes(new AtomicInteger(0))
                .disLikes(new AtomicInteger(0))
                .videoViewCounter(new AtomicInteger(0))
                .comments(Set.of(comment))
                .build();

        videoDTO = VideoDTO.builder()
                .username(user.getFullName())
                .title(video.getTitle())
                .description(video.getDescription())
                .videoUrl(video.getVideoUrl())
                .thumbnailUrl(video.getThumbnailUrl())
                .videoViewCounter(video.getVideoViewCounter())
                .likes(video.getLikes())
                .disLikes(video.getDisLikes())
                .build();

        when(videoDTOMapper.toDTO(video)).thenReturn(videoDTO);
    }


    @Test
    @WithMockUser(username = "user")
    public void testGetVideoDetailsWhenUserIsNotAuthenticated() throws Exception {
        when(userService.isUserAuthenticated(any())).thenReturn(false);
        when(videoService.getVideoDetails(anyInt(), any())).thenReturn(videoDTO);

        mockMvc.perform(get("/videos/{videoId}", videoId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.username").value(videoDTO.username()))
                .andExpect(jsonPath("$.title").value(videoDTO.title()))
                .andExpect(jsonPath("$.description").value(videoDTO.description()))
                .andExpect(jsonPath("$.videoUrl").value(videoDTO.videoUrl()))
                .andExpect(jsonPath("$.thumbnailUrl").value(videoDTO.thumbnailUrl()))
                .andExpect(jsonPath("$.videoViewCounter").value(videoDTO.videoViewCounter().get()))
                .andExpect(jsonPath("$.likes").value(videoDTO.likes().get()))
                .andExpect(jsonPath("$.disLikes").value(videoDTO.disLikes().get()))
                .andExpect(jsonPath("$.comments").value(videoDTO.comments()))
                .andReturn();
    }

    @Test
    @WithMockUser(username = "user")
    public void testGetVideoDetailsWhenUserIsAuthenticated() throws Exception {
        when(userService.isUserAuthenticated(any())).thenReturn(true);
        when(videoService.getVideoDetails(anyInt(), any())).thenReturn(videoDTO);

        mockMvc.perform(get("/videos/{videoId}", videoId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.username").value(videoDTO.username()))
                .andExpect(jsonPath("$.title").value(videoDTO.title()))
                .andExpect(jsonPath("$.description").value(videoDTO.description()))
                .andExpect(jsonPath("$.videoUrl").value(videoDTO.videoUrl()))
                .andExpect(jsonPath("$.thumbnailUrl").value(videoDTO.thumbnailUrl()))
                .andExpect(jsonPath("$.videoViewCounter").value(videoDTO.videoViewCounter().get()))
                .andExpect(jsonPath("$.likes").value(videoDTO.likes().get()))
                .andExpect(jsonPath("$.disLikes").value(videoDTO.disLikes().get()))
                .andExpect(jsonPath("$.comments").value(videoDTO.comments()))
                .andReturn();
    }

    @Test
    @WithMockUser(username = "user")
    public void testFindAllVideos() throws Exception {
        List<VideoDTO> videos = List.of(
          VideoDTO.builder().title("First Video").description("First Description").build(),
          VideoDTO.builder().title("Second Video").description("Second Description").build()
        );

        when(videoService.findAllVideos()).thenReturn(videos);

        mockMvc.perform(get("/videos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$[0].title").value("First Video"))
                .andExpect(jsonPath("$[1].title").value("Second Video"))
                .andExpect(jsonPath("$[0].description").value("First Description"))
                .andExpect(jsonPath("$[1].description").value("Second Description"))
                .andReturn();
    }
}
