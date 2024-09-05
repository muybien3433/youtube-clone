package com.muybien.youtube_clone.video;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muybien.youtube_clone.comment.CommentRequest;
import com.muybien.youtube_clone.comment.CommentService;
import com.muybien.youtube_clone.config.JwtService;
import com.muybien.youtube_clone.token.TokenRepository;
import com.muybien.youtube_clone.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VideoUserController.class)
public class VideoUserControllerTest {

    @MockBean private VideoService videoService;
    @MockBean private CommentService commentService;
    @MockBean private User user;
    @MockBean private Authentication connectedUser;
    @MockBean private JwtService jwtService;
    @MockBean private TokenRepository tokenRepository;
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private final int videoId = 1;
    private MockMultipartFile videoFile;
    private MockMultipartFile thumbnailFile;
    private VideoUploadResponse videoUploadResponse;
    private Video video;

    @BeforeEach
    public void setUp() {

        videoFile = new MockMultipartFile(
                "video", "video.mp4",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "video content".getBytes());
        thumbnailFile = new MockMultipartFile(
                "thumbnail", "thumbnail.jpg",
                MediaType.IMAGE_JPEG_VALUE, "thumbnail content".getBytes());

        videoUploadResponse = new VideoUploadResponse("http://mockurl.com");

        video = Video.builder()
                .user(user)
                .videoUrl("http://video-url.com")
                .thumbnailUrl("http://thumbnail-url.com")
                .title("Sample video")
                .description("Sample description")
                .likes(new AtomicInteger(0))
                .disLikes(new AtomicInteger(0))
                .videoViewCounter(new AtomicInteger(0))
                .createdDate(LocalDateTime.now())
                .build();

        VideoDTO videoDTO = VideoDTO.builder()
                .username(user.getFullName())
                .title(video.getTitle())
                .description(video.getDescription())
                .videoUrl(video.getVideoUrl())
                .thumbnailUrl(video.getThumbnailUrl())
                .videoViewCounter(video.getVideoViewCounter())
                .likes(video.getLikes())
                .disLikes(video.getDisLikes())
                .build();

        when(videoService.incrementVideoLike(anyInt(), any(Authentication.class))).thenReturn(videoDTO);
        when(videoService.incrementVideoDisLike(anyInt(), any(Authentication.class))).thenReturn(videoDTO);
    }

    @Test
    @WithMockUser(username = "user")
    public void testUploadVideoWhenUserIsAuthenticated() throws Exception {
        when(videoService.uploadVideo(any(MultipartFile.class), any(MultipartFile.class),
                anyString(), anyString(), any(Authentication.class))).thenReturn(videoUploadResponse);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/videos")
                        .file(videoFile)
                        .file(thumbnailFile)
                        .param("title", video.getTitle())
                        .param("description", video.getDescription())
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());
    }

    @Test
    public void testUploadVideoWhenUserIsNotAuthenticated() throws Exception {
        when(videoService.uploadVideo(any(MultipartFile.class), any(MultipartFile.class),
                anyString(), anyString(), any(Authentication.class))).thenReturn(videoUploadResponse);

        mockMvc.perform(multipart("/authorized/video/upload")
                        .file(videoFile)
                        .file(thumbnailFile)
                        .param("title", video.getTitle())
                        .param("description", video.getDescription())
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user")
    public void testLikeVideoWhenUserIsAuthenticated() throws Exception {
        mockMvc.perform(post("/videos/{videoId}/like", videoId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());

        verify(videoService, times(1)).incrementVideoLike(anyInt(), any(Authentication.class));
    }

    @Test
    public void testLikeVideoWhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(post("/videos/{videoId}/like", videoId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());

        verify(videoService, never()).incrementVideoLike(anyInt(), any(Authentication.class));
    }

    @Test
    @WithMockUser(username = "user")
    public void testDisLikeVideoWhenUserIsAuthenticated() throws Exception {
        mockMvc.perform(post("/videos/{videoId}/dislike", videoId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());

        verify(videoService, times(1)).incrementVideoDisLike(anyInt(), any(Authentication.class));
    }

    @Test
    public void testDisLikeVideoWhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(post("/videos/{videoId}/dislike", videoId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());

        verify(videoService, never()).incrementVideoDisLike(anyInt(), any(Authentication.class));
    }

    @Test
    @WithMockUser(username = "user")
    public void testAddCommentWhenUserIsAuthenticated() throws Exception {
        mockMvc.perform(post("/videos/{videoId}/comment", videoId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("videoId", String.valueOf(videoId))
                        .param("comment", "Sample Comment"))
                .andExpect(status().isOk());

        verify(commentService, times(1)).addComment(anyInt(), any(CommentRequest.class), any(Authentication.class));
    }

    @Test
    public void testAddCommentWhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(post("/videos/{videoId}/comment", videoId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("videoId", String.valueOf(videoId))
                        .param("comment", "Sample Comment"))
                .andExpect(status().isUnauthorized());

        verify(commentService, never()).addComment(anyInt(), any(CommentRequest.class), any(Authentication.class));
    }
}
