package com.muybien.youtube_clone.comment;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CommentDTOMapper {

    public CommentDTO toDTO(Comment comment) {
        return CommentDTO.builder()
                .authorName(comment.getAuthorName())
                .commentInput(comment.getCommentInput())
                .createdDate(LocalDateTime.now())
                .build();
    }

    public Set<CommentDTO> toDTOSet(Set<Comment> comments) {
        return comments.stream()
                .map(comment -> CommentDTO.builder()
                        .authorName(comment.getAuthorName())
                        .commentInput(comment.getCommentInput())
                        .createdDate(comment.getCreatedDate())
                        .build())
                .collect(Collectors.toSet());
    }
}
