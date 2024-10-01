package com.muybien.youtube_clone.video;

import com.muybien.youtube_clone.comment.Comment;
import com.muybien.youtube_clone.handler.DatabaseException;
import com.muybien.youtube_clone.handler.FileDeletionForbiddenException;
import com.muybien.youtube_clone.handler.InvalidFileUrlException;
import com.muybien.youtube_clone.handler.VideoNotFoundException;
import com.muybien.youtube_clone.s3aws.S3Service;
import com.muybien.youtube_clone.user.User;
import com.muybien.youtube_clone.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class VideoServiceTest {

    @Mock private UserService userService;
    @Mock private S3Service s3Service;
    @Mock private VideoRepository videoRepository;
    @Mock private VideoDTOMapper videoDTOMapper;
    @Mock private Authentication connectedUser;
    @InjectMocks private VideoService videoService;

    private final int videoId = 1;
    private Video video;
    private VideoDTO videoDTO;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        User user = User.builder()
                .firstname("Joe")
                .lastname("Smith")
                .email("joe.smith@gmail.com")
                .build();

        Comment comment = Comment.builder()
                .authorName(user.getFullName())
                .commentInput("Sample comment")
                .createdDate(LocalDateTime.now())
                .build();

        video = Video.builder()
                .user(user)
                .videoUrl("http://video-url.com")
                .thumbnailUrl("http://thumbnail-url.com")
                .title("Sample video")
                .description("Sample description")
                .likes(new AtomicInteger(0))
                .disLikes(new AtomicInteger(0))
                .videoViewCounter(new AtomicInteger(0))
                .comments(Set.of(comment))
                .createdDate(LocalDateTime.now())
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
    public void testGetVideoDetailsWhenSuccessAndUserIsAuthenticated() {
        when(userService.isUserAuthenticated(any())).thenReturn(true);
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));

        VideoDTO result = videoService.getVideoDetails(videoId, connectedUser);

        assertEquals(videoDTO, result);
        verify(userService, times(1)).addVideoToWatchedVideosHistory(videoId, connectedUser);
        verify(videoRepository, times(1)).findById(videoId);
        verify(videoRepository, times(1)).save(video);
    }

    @Test
    public void testGetVideoDetailsWhenSuccessAndUserIsNotAuthenticated() {
        when(userService.isUserAuthenticated(any())).thenReturn(false);
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));

        VideoDTO result = videoService.getVideoDetails(videoId, connectedUser);

        assertEquals(videoDTO, result);
        verify(userService, never()).addVideoToWatchedVideosHistory(videoId, connectedUser);
        verify(videoRepository, times(1)).findById(videoId);
        verify(videoRepository, times(1)).save(video);
    }

    @Test
    public void testGetVideoDetailsWhenFailure() {
        when(videoRepository.findById(videoId)).thenReturn(Optional.empty());

        assertThrows(VideoNotFoundException.class, () -> videoService.getVideoDetails(videoId, connectedUser));

        verify(userService, never()).addVideoToWatchedVideosHistory(videoId, connectedUser);
        verify(videoRepository, never()).save(any());
    }

    @Test
    public void testIncrementVideoLike() {
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));
        when(userService.isVideoLikedByUser(videoId, connectedUser)).thenReturn(false);
        when(userService.isVideoDisLikedByUser(videoId, connectedUser)).thenReturn(false);

        VideoDTO result = videoService.incrementVideoLike(videoId, connectedUser);

        assertEquals(videoDTO, result);
        verify(userService, times(1)).addVideoToLikedVideos(videoId, connectedUser);
        verify(videoRepository, times(1)).findById(videoId);
        verify(videoRepository, times(1)).save(video);
        verify(userService, never()).addVideoToDisLikedVideos(videoId, connectedUser);
    }

    @Test
    public void testIncrementVideoLikeWhenVideoAlreadyLiked() {
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));
        when(userService.isVideoLikedByUser(videoId, connectedUser)).thenReturn(true);
        when(userService.isVideoDisLikedByUser(videoId, connectedUser)).thenReturn(false);

        VideoDTO result = videoService.incrementVideoLike(videoId, connectedUser);

        assertEquals(videoDTO, result);
        verify(userService, times(1)).removeVideoFromLikedVideos(videoId, connectedUser);
        verify(videoRepository, times(1)).findById(videoId);
        verify(videoRepository, times(1)).save(video);
        verify(userService, never()).addVideoToLikedVideos(videoId, connectedUser);
        verify(userService, never()).addVideoToDisLikedVideos(videoId, connectedUser);
    }

    @Test
    public void testIncrementVideoLikeWhenVideoAlreadyDisliked() {
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));
        when(userService.isVideoLikedByUser(videoId, connectedUser)).thenReturn(false);
        when(userService.isVideoDisLikedByUser(videoId, connectedUser)).thenReturn(true);

        VideoDTO result = videoService.incrementVideoLike(videoId, connectedUser);

        assertEquals(videoDTO, result);
        verify(userService, times(1)).addVideoToLikedVideos(videoId, connectedUser);
        verify(videoRepository, times(1)).findById(videoId);
        verify(videoRepository, times(1)).save(video);
        verify(userService, never()).addVideoToDisLikedVideos(videoId, connectedUser);
        verify(userService, never()).removeVideoFromLikedVideos(videoId, connectedUser);
    }

    @Test
    public void testIncrementVideoDisLike() {
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));
        when(userService.isVideoLikedByUser(videoId, connectedUser)).thenReturn(false);
        when(userService.isVideoDisLikedByUser(videoId, connectedUser)).thenReturn(false);

        VideoDTO result = videoService.incrementVideoDisLike(videoId, connectedUser);

        assertEquals(videoDTO, result);
        verify(userService, times(1)).addVideoToDisLikedVideos(videoId, connectedUser);
        verify(videoRepository, times(1)).findById(videoId);
        verify(videoRepository, times(1)).save(video);
        verify(userService, never()).addVideoToLikedVideos(videoId, connectedUser);
        verify(userService, never()).removeVideoFromDisLikedVideos(videoId, connectedUser);
    }

    @Test
    public void testIncrementVideoDisLikeWhenVideoAlreadyDisliked() {
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));
        when(userService.isVideoLikedByUser(videoId, connectedUser)).thenReturn(false);
        when(userService.isVideoDisLikedByUser(videoId, connectedUser)).thenReturn(true);

        VideoDTO result = videoService.incrementVideoDisLike(videoId, connectedUser);

        assertEquals(videoDTO, result);
        verify(userService, times(1)).removeVideoFromDisLikedVideos(videoId, connectedUser);
        verify(videoRepository, times(1)).findById(videoId);
        verify(videoRepository, times(1)).save(video);
        verify(userService, never()).addVideoToLikedVideos(videoId, connectedUser);
        verify(userService, never()).addVideoToDisLikedVideos(videoId, connectedUser);
    }

    @Test
    public void testIncrementVideoDisLikeWhenVideoAlreadyLiked() {
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));
        when(userService.isVideoLikedByUser(videoId, connectedUser)).thenReturn(true);
        when(userService.isVideoDisLikedByUser(videoId, connectedUser)).thenReturn(false);

        VideoDTO result = videoService.incrementVideoDisLike(videoId, connectedUser);

        assertEquals(videoDTO, result);
        verify(userService, times(1)).removeVideoFromLikedVideos(videoId, connectedUser);
        verify(userService, times(1)).addVideoToDisLikedVideos(videoId, connectedUser);
        verify(videoRepository, times(1)).findById(videoId);
        verify(videoRepository, times(1)).save(video);
        verify(userService, never()).addVideoToLikedVideos(videoId, connectedUser);
        verify(userService, never()).removeVideoFromDisLikedVideos(videoId, connectedUser);
    }

    @Test
    public void testUploadVideoWhenSuccess() {
        var videoFile = mock(MultipartFile.class);
        var thumbnailFile = mock(MultipartFile.class);

        when(s3Service.uploadFileAndFetchFileUrl(thumbnailFile)).thenReturn("http://thumbnail-url.com");
        when(s3Service.uploadFileAndFetchFileUrl(videoFile)).thenReturn("http://video-url.com");
        when(videoRepository.save(any(Video.class))).thenReturn(video);

        VideoUploadResponse result = videoService.uploadVideo(
                videoFile, thumbnailFile, "Title", "Description", connectedUser);

        assertEquals("http://video-url.com", result.videoUrl());
        verify(s3Service, times(1)).uploadFileAndFetchFileUrl(videoFile);
        verify(s3Service, times(1)).uploadFileAndFetchFileUrl(thumbnailFile);
        verify(videoRepository, times(1)).save(any(Video.class));
        verify(userService, times(1)).sendNotificationToSubscribers(connectedUser);

    }

    @Test
    public void testUploadVideoWhenInvalidReturnedUrl() {
        var videoFile = mock(MultipartFile.class);
        var thumbnailFile = mock(MultipartFile.class);

        when(s3Service.uploadFileAndFetchFileUrl(videoFile)).thenReturn("hs:/invalid-url.com");
        when(s3Service.uploadFileAndFetchFileUrl(thumbnailFile)).thenReturn("hs:/invalid-url.com");

        assertThrows(InvalidFileUrlException.class, () ->
                videoService.uploadVideo(videoFile, thumbnailFile, "Title", "Description", connectedUser));

        verify(videoRepository, never()).save(any(Video.class));
    }

    @Test
    public void testUploadVideoWhenDatabaseException() {
        String videoUrl = "http://video-url.com";
        String thumbnailUrl = "http://thumbnail-url.com";
        var videoFile = mock(MultipartFile.class);
        var thumbnailFile = mock(MultipartFile.class);

        when(s3Service.uploadFileAndFetchFileUrl(videoFile)).thenReturn("http://video-url.com");
        when(s3Service.uploadFileAndFetchFileUrl(thumbnailFile)).thenReturn("http://thumbnail-url.com");

        doThrow(new DataAccessException("Database error") {}).when(videoRepository).save(any(Video.class));
        videoService.deleteVideoAndThumbnailFromS3(videoUrl, thumbnailUrl);

        assertThrows(DatabaseException.class, () ->
                videoService.uploadVideo(videoFile, thumbnailFile, "Title", "Description", connectedUser));
        verify(s3Service, times(2)).deleteFileFromS3(videoUrl); // 2 times because bc video and thumb delete separately
    }

    @Test
    public void testDeleteVideo() {
        var user =  User.builder().email("joe.smith@gmail.com").build();

        when(videoRepository.findById(videoId)).thenReturn(Optional.ofNullable(video));
        when(connectedUser.getPrincipal()).thenReturn(user);

        videoService.deleteVideo(videoId, connectedUser);

        verify(videoRepository, times(1)).findById(videoId);
        verify(videoRepository, times(1)).delete(video);
    }

    @Test
    public void testDeleteVideoWhenUserIsNotVideoOwner() {
        var user =  User.builder().build();

        when(videoRepository.findById(videoId)).thenReturn(Optional.ofNullable(video));
        when(connectedUser.getPrincipal()).thenReturn(user);

        assertThrows(FileDeletionForbiddenException.class, () ->
                videoService.deleteVideo(videoId, connectedUser));

        verify(videoRepository, times(1)).findById(videoId);
        verify(videoRepository, never()).delete(video);
    }

    @Test
    public void findAllVideos() {
        when(videoRepository.findAll()).thenReturn(List.of(video));
        when(videoDTOMapper.toDTO(video)).thenReturn(videoDTO);

        List<VideoDTO> result = videoService.findAllVideos();

        assertEquals(1, result.size());
        assertEquals(videoDTO, result.getFirst());
        verify(videoRepository, times(1)).findAll();
    }
}