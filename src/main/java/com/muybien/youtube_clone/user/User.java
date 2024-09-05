package com.muybien.youtube_clone.user;

import com.muybien.youtube_clone.comment.Comment;
import com.muybien.youtube_clone.common.BaseEntity;
import com.muybien.youtube_clone.video.Video;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@SuperBuilder
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "users")
public class User extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String firstname;
    private String lastname;
    @Column(unique = true, nullable = false)
    private String email;
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Integer> watchedVideoHistory = new LinkedList<>();
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Integer> likedVideos = new HashSet<>();
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Integer> disLikedVideos =  new HashSet<>();
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Integer> userSubscribedTo = new HashSet<>();
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Integer> userSubscribedBy = new HashSet<>();
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> notifications = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Video> videos = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    public void addVideoToWatchedVideosHistory(Integer videoId) {
        watchedVideoHistory.add(videoId);
    }

    public void removeVideoFromWatchedVideosHistory(Integer videoId) {
        watchedVideoHistory.remove(videoId);
    }

    public void addVideoToLikedVideos(Integer videoId) {
        likedVideos.add(videoId);
    }

    public void removeVideoFromLikedVideos(Integer videoId) {
        likedVideos.remove(videoId);
    }

    public void addVideoToDisLikedVideos(Integer videoId) {
        disLikedVideos.add(videoId);
    }

    public void removeVideoFromDisLikedVideos(Integer videoId) {
        disLikedVideos.remove(videoId);
    }

    public void addUserToSubscribedTo(Integer userId) {
        userSubscribedTo.add(userId);
    }

    public void removeUserFromSubscribedTo(Integer userId) {
        userSubscribedTo.remove(userId);
    }

    public void addUserToSubscribedBy(Integer userId) {
        userSubscribedBy.add(userId);
    }

    public void removeUserFromSubscribedBy(Integer userId) {
        userSubscribedBy.remove(userId);
    }

    public void addUserToNotifications(String notification) {
        notifications.add(notification);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return email;
    }

    public String getFullName() {
        return String.format("%s %s", firstname, lastname);
    }
}
