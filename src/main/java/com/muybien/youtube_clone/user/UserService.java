package com.muybien.youtube_clone.user;

import com.muybien.youtube_clone.handler.InvalidSubscriptionException;
import com.muybien.youtube_clone.handler.SubscriptionException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public boolean isUserAuthenticated(Authentication connectedUser) {
        return connectedUser != null &&
                connectedUser.isAuthenticated() &&
                connectedUser.getPrincipal() instanceof UserDetails;
    }

    @Transactional
    public void addVideoToWatchedVideosHistory(Integer videoId, Authentication connectedUser) {
        var user = (User) connectedUser.getPrincipal();
        user.removeVideoFromWatchedVideosHistory(videoId);
        user.addVideoToWatchedVideosHistory(videoId);
        userRepository.save(user);
    }

    @Transactional
    public void addVideoToLikedVideos(Integer videoId, Authentication connectedUser) {
        var user = (User) connectedUser.getPrincipal();
        user.addVideoToLikedVideos(videoId);
        userRepository.save(user);
    }

    @Transactional
    public void removeVideoFromLikedVideos(Integer videoId, Authentication connectedUser) {
        var user = (User) connectedUser.getPrincipal();
        user.removeVideoFromLikedVideos(videoId);
        userRepository.save(user);
    }

    @Transactional
    public void addVideoToDisLikedVideos(Integer videoId, Authentication connectedUser) {
        var user = (User) connectedUser.getPrincipal();
        user.addVideoToDisLikedVideos(videoId);
        userRepository.save(user);
    }

    @Transactional
    public void removeVideoFromDisLikedVideos(Integer videoId, Authentication connectedUser) {
        var user = (User) connectedUser.getPrincipal();
        user.removeVideoFromDisLikedVideos(videoId);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public boolean isVideoLikedByUser(Integer videoId, Authentication connectedUser) {
        var user = (User) connectedUser.getPrincipal();
        return user.getLikedVideos().stream().anyMatch(likedVideo -> likedVideo.equals(videoId));
    }

    @Transactional(readOnly = true)
    public boolean isVideoDisLikedByUser(Integer videoId, Authentication connectedUser) {
        var user = (User) connectedUser.getPrincipal();
        return user.getDisLikedVideos().stream().anyMatch(disLikedVideo -> disLikedVideo.equals(videoId));
    }

    // method returns reversed LinkedList to obtain
    // recent watched video from latest to oldest
    @Transactional(readOnly = true)
    public List<Integer> getUserWatchVideosHistory(Authentication connectedUser) {
        var user = (User) connectedUser.getPrincipal();
        return user.getWatchedVideoHistory().reversed();
    }

    @Transactional
    public void toggleUserSubscription(Integer targetUserId, Authentication connectedUser) {
        boolean isTargetAlreadySubscribed = isSubscribedByUser(targetUserId, connectedUser);

        if (isTargetAlreadySubscribed) {
            removeSubscription(targetUserId, connectedUser);
        } else {
            addSubscription(targetUserId, connectedUser);
        }
    }

    private void addSubscription(Integer targetUserId, Authentication connectedUser) {
        var user = (User) connectedUser.getPrincipal();
        var targetUser = validateAndFetchSubscriptionTarget(targetUserId, connectedUser);

        try {
            user.addUserToSubscribedTo(targetUserId);
            targetUser.addUserToSubscribedBy(user.getId());
            userRepository.saveAll(List.of(user, targetUser));
        } catch (Exception e) {
            throw new SubscriptionException("Filed to add subscription.", e);
        }
    }

    private void removeSubscription(Integer targetUserId, Authentication connectedUser) {
        var user = (User) connectedUser.getPrincipal();
        var targetUser = validateAndFetchSubscriptionTarget(targetUserId, connectedUser);

        try {
            user.removeUserFromSubscribedTo(targetUserId);
            targetUser.removeUserFromSubscribedBy(user.getId());
            userRepository.saveAll(List.of(user, targetUser));
        } catch (Exception e) {
            throw new SubscriptionException("Failed to remove subscription", e);
        }
    }

    private boolean isSubscribedByUser(Integer targetUserId, Authentication connectedUser) {
        var user = (User) connectedUser.getPrincipal();
        return user.getUserSubscribedTo() != null && user.getUserSubscribedTo().contains(targetUserId);
    }

    private User validateAndFetchSubscriptionTarget(Integer targetUserId, Authentication connectedUser) {
        var user = (User) connectedUser.getPrincipal();

        if (targetUserId.equals(user.getId())) {
            throw new InvalidSubscriptionException("You can't subscribe to yourself.");
        }
        return userRepository.findById(targetUserId)
                .orElseThrow(() -> new InvalidSubscriptionException("User doesn't exist."));
    }

    @Transactional
    public void sendNotificationToSubscribers(Authentication connectedUser) {
        var user = (User) connectedUser.getPrincipal();
        Set<Integer> subscribers = user.getUserSubscribedBy();

        if (!subscribers.isEmpty()) {
            subscribers.forEach(subscriber -> userRepository.findById(subscriber).ifPresent(presentSubscriber -> {
                String message = "User " + user.getFullName() + " has posted a new video!";
                presentSubscriber.addUserToNotifications(message);
            }));
        }
    }
}