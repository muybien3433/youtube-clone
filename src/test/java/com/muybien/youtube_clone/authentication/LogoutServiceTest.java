package com.muybien.youtube_clone.authentication;

import com.muybien.youtube_clone.token.Token;
import com.muybien.youtube_clone.token.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class LogoutServiceTest {

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private Authentication connectedUser;
    @Mock private TokenRepository tokenRepository;
    @InjectMocks private LogoutService logoutService;

    private final String jwtToken = "token";
    private final String authHeader = "Bearer " + jwtToken;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLogoutWhenTokenIsValid() {
        var storedToken = Token.builder().expired(false).build();

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(storedToken));

        logoutService.logout(request, response, connectedUser);

        verify(tokenRepository, times(1)).save(storedToken);
        verify(tokenRepository, times(1)).findByToken(anyString());
        assertTrue(storedToken.isExpired());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testLogoutWhenTokenIsInvalid() {
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

        logoutService.logout(request, response, connectedUser);

        verify(tokenRepository, never()).findByToken(anyString());
        verify(tokenRepository, never()).save(any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testLogoutWhenTokenNotFound() {
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(tokenRepository.findByToken(jwtToken)).thenReturn(Optional.empty());

        logoutService.logout(request, response, connectedUser);

        verify(tokenRepository, times(1)).findByToken(jwtToken);
        verify(tokenRepository, never()).save(any());
        assertNull( SecurityContextHolder.getContext().getAuthentication());
    }
}
