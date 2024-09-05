package com.muybien.youtube_clone.video;

import com.muybien.youtube_clone.comment.Comment;
import com.muybien.youtube_clone.common.BaseEntity;
import com.muybien.youtube_clone.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
@SuperBuilder
@Getter
@Setter
@RequiredArgsConstructor
public class Video extends BaseEntity {

    @Id
    @GeneratedValue
    private Integer id;
    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;
    private AtomicInteger videoViewCounter;
    private AtomicInteger likes;
    private AtomicInteger disLikes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    public void incrementVideoViewCounter() {
        videoViewCounter.incrementAndGet();
    }

    public void incrementVideoLikeCounter() {
        likes.incrementAndGet();
    }

    public void decrementVideoLikesCounter() {
        likes.decrementAndGet();
    }

    public void incrementVideoDisLikeCounter() {
        disLikes.incrementAndGet();
    }

    public void decrementVideoDisLikeCounter() {
        disLikes.decrementAndGet();
    }

}
