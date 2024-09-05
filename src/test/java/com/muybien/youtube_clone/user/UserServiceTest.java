package com.muybien.youtube_clone.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private User user;
    @Mock private Authentication connectedUser;
    @InjectMocks private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(connectedUser.getPrincipal()).thenReturn(user);
    }

    @Test
    public void testIsUserAuthenticatedWhenUserIsAuthenticated() {
        when(connectedUser.isAuthenticated()).thenReturn(true);

        boolean result = userService.isUserAuthenticated(connectedUser);

        assertTrue(result);
    }

    @Test
    public void testIsUserAuthenticatedWhenUserIsNotAuthenticated() {
        when(connectedUser.isAuthenticated()).thenReturn(false);

        boolean result = userService.isUserAuthenticated(connectedUser);

        assertFalse(result);
    }

    @Test
    public void testAddVideoToWatchedVideosHistory() {
        int videoId = 1;

        userService.addVideoToWatchedVideosHistory(videoId, connectedUser);

        verify(user, times(1)).removeVideoFromWatchedVideosHistory(videoId);
        verify(user, times(1)).addVideoToWatchedVideosHistory(videoId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testAddVideoToLikedVideos() {
        int videoId = 1;

        userService.addVideoToLikedVideos(videoId, connectedUser);

        verify(user, times(1)).addVideoToLikedVideos(videoId);
        verify(user, never()).removeVideoFromLikedVideos(videoId);
        verify(userRepository, times(1)).save(user);

    }

    @Test
    public void testRemoveVideoFromLikedVideos() {
        int videoId = 1;

        userService.removeVideoFromLikedVideos(videoId, connectedUser);

        verify(user, times(1)).removeVideoFromLikedVideos(videoId);
        verify(user, never()).addVideoToLikedVideos(videoId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testAddVideoToDisLikedVideos() {
        int videoId = 1;

        userService.addVideoToDisLikedVideos(videoId, connectedUser);

        verify(user, times(1)).addVideoToDisLikedVideos(videoId);
        verify(user, never()).removeVideoFromDisLikedVideos(videoId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testRemoveVideoFromDisLikedVideos() {
        int videoId = 1;

        userService.removeVideoFromDisLikedVideos(videoId, connectedUser);

        verify(user, times(1)).removeVideoFromDisLikedVideos(videoId);
        verify(user, never()).addVideoToDisLikedVideos(videoId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testIsVideoLikedByUser() {
        int videoId = 1;

        when(user.getLikedVideos()).thenReturn(Set.of(videoId));

        boolean result = userService.isVideoLikedByUser(videoId, connectedUser);

        assertTrue(result);
    }

    @Test
    public void testIsVideoDisLikedByUser() {
        int videoId = 1;

        when(user.getDisLikedVideos()).thenReturn(Set.of(videoId));

        boolean result = userService.isVideoDisLikedByUser(videoId, connectedUser);

        assertTrue(result);
    }

    @Test
    public void testGetUserVideosHistory() {
        // method returns reversed LinkedList to obtain
        // recent watched video from latest to oldest
        var expectedVideoHistory = Arrays.asList(1001, 1003, 1005);
        var videoHistory = Arrays.asList(1005, 1003, 1001);

        when(user.getWatchedVideoHistory()).thenReturn(videoHistory);

        var result = userService.getUserWatchVideosHistory(connectedUser);

        assertEquals(expectedVideoHistory, result);
        verify(user, times(1)).getWatchedVideoHistory();
    }

    @Test
    public void testToggleUserSubscriptionWhenUserNotSubscribed() {
        int targetUserId = 1;
        var targetUser = mock(User.class);

        when(user.getUserSubscribedTo()).thenReturn(new HashSet<>());
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));

        userService.toggleUserSubscription(targetUserId, connectedUser);

        verify(user, times(1)).addUserToSubscribedTo(targetUserId);
        verify(targetUser, times(1)).addUserToSubscribedBy(user.getId());
        verify(userRepository, times(1)).saveAll(List.of(user, targetUser));
        verify(user, never()).removeUserFromSubscribedTo(targetUserId);
        verify(targetUser, never()).removeUserFromSubscribedBy(user.getId());
    }

    @Test
    public void testToggleUserSubscriptionWhenUserAlreadySubscribed() {
        int targetUserId = 1;
        var targetUser = mock(User.class);
        Set<Integer> subscribers = new HashSet<>();
        subscribers.add(targetUserId);

        when(user.getUserSubscribedTo()).thenReturn(subscribers);
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));

        userService.toggleUserSubscription(targetUserId, connectedUser);

        verify(user, times(1)).removeUserFromSubscribedTo(targetUserId);
        verify(targetUser, times(1)).removeUserFromSubscribedBy(user.getId());
        verify(userRepository, times(1)).saveAll(List.of(user, targetUser));
        verify(user, never()).addUserToSubscribedTo(targetUserId);
        verify(targetUser, never()).addUserToSubscribedBy(user.getId());
    }

    @Test
    public void testSendNotificationToSubscribers() {
        int subscriberId = 1;
        var subscriber = mock(User.class);

        when(user.getUserSubscribedBy()).thenReturn(Set.of(subscriberId));
        when(userRepository.findById(subscriberId)).thenReturn(Optional.of(subscriber));
        when(user.getFullName()).thenReturn("John Doe");

        userService.sendNotificationToSubscribers(connectedUser);

        String expectedMessage = "User John Doe has posted a new video!";
        verify(subscriber).addUserToNotifications(expectedMessage);
    }
}