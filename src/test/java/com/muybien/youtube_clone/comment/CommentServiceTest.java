package com.muybien.youtube_clone.comment;

import com.muybien.youtube_clone.user.User;
import com.muybien.youtube_clone.video.Video;
import com.muybien.youtube_clone.video.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class CommentServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private CommentDTOMapper commentDTOMapper;
    @Mock private VideoService videoService;
    @Mock private Authentication connectedUser;
    @InjectMocks private CommentService commentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddComment() {
        int videoId = 1;
        var user = new User();
        var video = new Video();
        var commentRequest = new CommentRequest("comment-input");

        when(connectedUser.getPrincipal()).thenReturn(user);
        when(videoService.findVideoById(videoId)).thenReturn(video);

        commentService.addComment(videoId, commentRequest, connectedUser);

        verify(videoService, times(1)).findVideoById(anyInt());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    public void testGetAllCommentsByVideoId() {
        int videoId = 1;
        var comment1 = new Comment();
        var comment2 = new Comment();
        var video = new Video();
        video.setComments(Set.of(comment1, comment2));

        var commentDTO1 = new CommentDTO("author1",
                "comment-input1", LocalDateTime.now());
        var commentDTO2 = new CommentDTO("author2",
                "comment-input2", LocalDateTime.now());

        when(videoService.findVideoById(videoId)).thenReturn(video);
        when(commentDTOMapper.toDTO(comment1)).thenReturn(commentDTO1);
        when(commentDTOMapper.toDTO(comment2)).thenReturn(commentDTO2);

        var result = commentService.getAllCommentsByVideoId(videoId);

        assertEquals(2, result.size());
        assertTrue(result.contains(commentDTO1));
        assertTrue(result.contains(commentDTO2));

        verify(videoService, times(1)).findVideoById(anyInt());
        verify(commentDTOMapper, times(1)).toDTO(comment1);
        verify(commentDTOMapper, times(1)).toDTO(comment2);
    }
}