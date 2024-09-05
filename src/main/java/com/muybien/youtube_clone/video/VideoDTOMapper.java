package com.muybien.youtube_clone.video;

import com.muybien.youtube_clone.comment.CommentDTOMapper;
import org.springframework.stereotype.Component;

@Component
public class VideoDTOMapper {

    private final CommentDTOMapper commentDTOMapper;

    public VideoDTOMapper(CommentDTOMapper commentDTOMapper) {
        this.commentDTOMapper = commentDTOMapper;
    }

    public VideoDTO toDTO(Video video) {
        return VideoDTO.builder()
                .username(video.getUser().getFullName())
                .videoUrl(video.getVideoUrl())
                .thumbnailUrl(video.getThumbnailUrl())
                .title(video.getTitle())
                .description(video.getDescription())
                .videoViewCounter(video.getVideoViewCounter())
                .likes(video.getLikes())
                .disLikes(video.getDisLikes())
                .comments(commentDTOMapper.toDTOSet(video.getComments()))
                .build();
    }
}