package com.muybien.youtube_clone.video;

import com.muybien.youtube_clone.handler.DatabaseException;
import com.muybien.youtube_clone.handler.InvalidFileUrlException;
import com.muybien.youtube_clone.handler.VideoNotFoundException;
import com.muybien.youtube_clone.s3aws.S3Service;
import com.muybien.youtube_clone.user.User;
import com.muybien.youtube_clone.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final VideoDTOMapper videoDTOMapper;
    private final UserService userService;
    private final S3Service s3Service;

    @Transactional
    public VideoDTO getVideoDetails(Integer videoId, Authentication connectedUser) {
        var fetchedVideo = findVideoById(videoId);

        if (userService.isUserAuthenticated(connectedUser)) {
            userService.addVideoToWatchedVideosHistory(videoId, connectedUser);
        }
        incrementVideoViewCounter(fetchedVideo);

        return videoDTOMapper.toDTO(fetchedVideo);
    }

    private void incrementVideoViewCounter(Video video) {
        video.incrementVideoViewCounter();
        videoRepository.save(video);
    }

    @Transactional
    public VideoDTO incrementVideoLike(Integer videoId, Authentication connectedUser) {
        var fetchedVideo = findVideoById(videoId);

        if (isVideoLikedByUser(videoId, connectedUser)) {
            fetchedVideo.decrementVideoLikesCounter();
            userService.removeVideoFromLikedVideos(videoId, connectedUser);
        } else {
            if (isVideoDisLikedByUser(videoId, connectedUser)) {
                fetchedVideo.decrementVideoDisLikeCounter();
                userService.removeVideoFromDisLikedVideos(videoId, connectedUser);
            }
            fetchedVideo.incrementVideoLikeCounter();
            userService.addVideoToLikedVideos(videoId, connectedUser);
        }
        videoRepository.save(fetchedVideo);
        return videoDTOMapper.toDTO(fetchedVideo);
    }

    @Transactional
    public VideoDTO incrementVideoDisLike(Integer videoId, Authentication connectedUser) {
        var fetchedVideo = findVideoById(videoId);

        if (isVideoDisLikedByUser(videoId, connectedUser)) {
            fetchedVideo.decrementVideoDisLikeCounter();
            userService.removeVideoFromDisLikedVideos(videoId, connectedUser);
        } else {
            if (isVideoLikedByUser(videoId, connectedUser)) {
                fetchedVideo.decrementVideoLikesCounter();
                userService.removeVideoFromLikedVideos(videoId, connectedUser);
            }
            fetchedVideo.incrementVideoDisLikeCounter();
            userService.addVideoToDisLikedVideos(videoId, connectedUser);
        }
        videoRepository.save(fetchedVideo);
        return videoDTOMapper.toDTO(fetchedVideo);
    }


    private boolean isVideoLikedByUser(Integer videoId, Authentication connectedUser) {
        return userService.isVideoLikedByUser(videoId, connectedUser);
    }

    private boolean isVideoDisLikedByUser(Integer videoId, Authentication connectedUser) {
        return userService.isVideoDisLikedByUser(videoId, connectedUser);
    }

    @Transactional(readOnly = true)
    public Video findVideoById(Integer videoId) {
        return videoRepository.findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException(
                        "Video with ID: " + videoId + " doesn't exist."));
    }

    @Transactional
    public VideoUploadResponse uploadVideo(MultipartFile videoFile,
                                           MultipartFile thumbnailFile,
                                           String title,
                                           String description,
                                           Authentication connectedUser) {

        var fetchedUser = (User) connectedUser.getPrincipal();
        String videoUrl = s3Service.uploadFileAndFetchFileUrl(videoFile);
        String thumbnailUrl = s3Service.uploadFileAndFetchFileUrl(thumbnailFile);

        var video = Video.builder()
                .user(fetchedUser)
                .videoUrl(videoUrl)
                .thumbnailUrl(thumbnailUrl)
                .title(title)
                .description(description)
                .likes(new AtomicInteger(0))
                .disLikes(new AtomicInteger(0))
                .videoViewCounter(new AtomicInteger(0))
                .createdDate(LocalDateTime.now())
                .build();

        saveVideo(video, videoUrl, thumbnailUrl, connectedUser);
        return new VideoUploadResponse(video.getVideoUrl());
    }

    private void saveVideo(Video video, String videoUrl, String thumbnailUrl, Authentication connectedUser) {
        try {
            isVideoAndThumbnailUrlValid(videoUrl, thumbnailUrl);
            videoRepository.save(video);
            userService.sendNotificationToSubscribers(connectedUser);
        } catch (DataAccessException e) {
            deleteVideoAndThumbnailFromS3(videoUrl, thumbnailUrl);
            throw new DatabaseException("Failed to save video to database", e);
        }
    }

    private void isVideoAndThumbnailUrlValid(String videoUrl, String thumbnailUrl) {
         if (isUrlNonValid(videoUrl) || isUrlNonValid(thumbnailUrl)) {
             throw new InvalidFileUrlException("Invalid video or/and thumbnail URL");
         }
    }

    private boolean isUrlNonValid(String videoUrl) {
        return !videoUrl.startsWith("http");
    }

    void deleteVideoAndThumbnailFromS3(String videoUrl, String thumbnailUrl) {
        deleteFileFromS3(videoUrl);
        deleteFileFromS3(thumbnailUrl);
    }

    private void deleteFileFromS3(String fileUrl) {
        s3Service.deleteFileFromS3(fileUrl);
    }

    @Transactional(readOnly = true)
    public List<VideoDTO> findAllVideos() {
        return videoRepository.findAll().stream()
                .map(videoDTOMapper::toDTO).toList();
    }
}