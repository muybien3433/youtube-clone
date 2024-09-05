package com.muybien.youtube_clone.user;

import com.muybien.youtube_clone.config.JwtService;
import com.muybien.youtube_clone.token.TokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @MockBean private UserService userService;
    @MockBean private Authentication connectedUser;
    @MockBean private JwtService jwtService;
    @MockBean private TokenRepository tokenRepository;
    @Autowired private MockMvc mockMvc;

    private final int targetUserId = 1;

    @Test
    @WithMockUser(username = "user")
    public void testGetUserHistory() throws Exception {
        List<Integer> history = List.of(1, 2, 3, 4, 5);
        when(userService.getUserWatchVideosHistory(connectedUser)).thenReturn(history);

        mockMvc.perform(get("/users/history"))
                        .andExpect(status().isOk());

        verify(userService, times(1)).getUserWatchVideosHistory(any(Authentication.class));
    }

    @Test
    @WithMockUser(username = "user")
    public void testGetUserHistoryWhenEmpty() throws Exception {
        when(userService.getUserWatchVideosHistory(connectedUser)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users/history"))
                        .andExpect(status().isOk());

        verify(userService, times(1)).getUserWatchVideosHistory(any(Authentication.class));
    }

    @Test
    public void testGetUserHistoryWhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/users/history"))
                        .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user")
    public void testToggleSubscriptionWhenUserIsAuthenticated() throws Exception {
        mockMvc.perform(post("/users/toggle/subscribe/{targetUserId}", targetUserId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                        .andExpect(status().isOk());

        verify(userService, times(1)).toggleUserSubscription(anyInt(), any(Authentication.class));
    }

    @Test
    public void testToggleSubscriptionWhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(post("/users/toggle/subscribe/{targetUserId}", targetUserId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                        .andExpect(status().isUnauthorized());

        verify(userService, never()).toggleUserSubscription(anyInt(), any(Authentication.class));
    }
}
