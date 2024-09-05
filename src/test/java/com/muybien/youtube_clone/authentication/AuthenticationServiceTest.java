package com.muybien.youtube_clone.authentication;

import com.muybien.youtube_clone.config.JwtService;
import com.muybien.youtube_clone.handler.EmailAlreadyTakenException;
import com.muybien.youtube_clone.handler.UserNotFoundException;
import com.muybien.youtube_clone.token.Token;
import com.muybien.youtube_clone.token.TokenRepository;
import com.muybien.youtube_clone.user.User;
import com.muybien.youtube_clone.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuthenticationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private TokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @InjectMocks private AuthenticationService authenticationService;

    private User user;
    private RegisterRequest registerRequest;
    private AuthenticationRequest authenticationRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password("password")
                .createdDate(LocalDateTime.now())
                .build();

        registerRequest = RegisterRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password("password")
                .build();

        authenticationRequest = AuthenticationRequest.builder()
                .email("john.doe@example.com")
                .password("encoded-password")
                .build();
    }

    @Test
    public void testRegister() {
        String jwtToken = "token";

        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(user.getEmail())).thenReturn(jwtToken);

        AuthenticationResponse response = authenticationService.register(registerRequest);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtService, times(1)).generateToken(user.getEmail());
        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @Test
    public void testRegisterWhenThrowsDatabaseException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");

        doThrow(new RuntimeException("Database error")).when(userRepository).save(any(User.class));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(tokenRepository);
    }

    @Test
    public void testRegisterWhenThrowsEmailAlreadyTakenException() {
        var existingUser = User.builder().email("john.doe@example.com").build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(existingUser));

        assertThrows(EmailAlreadyTakenException.class, () -> authenticationService.register(registerRequest));
        verifyNoInteractions(jwtService);
        verifyNoInteractions(tokenRepository);
    }

    @Test
    public void testAuthenticate() {
        String jwtToken = "token";

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user.getEmail())).thenReturn(jwtToken);

        AuthenticationResponse response = authenticationService.authenticate(authenticationRequest);

        assertNotNull(response);
        verify(userRepository, times(1)).findByEmail("john.doe@example.com");
        verify(jwtService, times(1)).generateToken(user.getUsername());
        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @Test
    public void testAuthenticateWhenThrowsUserNotFoundException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authenticationService.authenticate(authenticationRequest));
        verify(userRepository, times(1)).findByEmail(anyString());
        verifyNoInteractions(jwtService);
        verifyNoInteractions(tokenRepository);
    }
}
