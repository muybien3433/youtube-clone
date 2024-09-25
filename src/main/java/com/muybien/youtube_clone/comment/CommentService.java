package com.muybien.youtube_clone.comment;

import com.muybien.youtube_clone.user.User;
import com.muybien.youtube_clone.video.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentDTOMapper commentDTOMapper;
    private final VideoService videoService;

    @Transactional
    public void addComment(Integer videoId, CommentRequest request, Authentication connectedUser) {
        var user = (User) connectedUser.getPrincipal();
        var video = videoService.findVideoById(videoId);
        var comment = Comment.builder()
                .authorName(user.getFullName())
                .user(user)
                .video(video)
                .commentInput(request.commentInput())
                .createdDate(LocalDateTime.now())
                .build();

        commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> getAllCommentsByVideoId(Integer videoId) {
        var video = videoService.findVideoById(videoId);
        Set<Comment> comments = video.getComments();

        return comments.stream().map(commentDTOMapper::toDTO).toList();
    }
}
