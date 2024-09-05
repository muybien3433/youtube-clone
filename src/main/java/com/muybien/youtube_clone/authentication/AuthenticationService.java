package com.muybien.youtube_clone.authentication;

import com.muybien.youtube_clone.config.JwtService;
import com.muybien.youtube_clone.handler.EmailAlreadyTakenException;
import com.muybien.youtube_clone.handler.UserNotFoundException;
import com.muybien.youtube_clone.user.User;
import com.muybien.youtube_clone.user.UserRepository;
import com.muybien.youtube_clone.token.Token;
import com.muybien.youtube_clone.token.TokenRepository;
import com.muybien.youtube_clone.token.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtService jwtService;
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyTakenException("Email is already taken.");
        }

        var user = User.builder()
                .firstname(request.firstname())
                .lastname(request.lastname())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .createdDate(LocalDateTime.now())
                .build();

        var savedUser = repository.save(user);
        var jwtToken = jwtService.generateToken(user.getEmail());
        saveUserToken(savedUser, jwtToken);

        return new AuthenticationResponse(jwtToken);
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = repository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found, email: " + request.email()));

        var jwtToken = jwtService.generateToken(user.getEmail());
        saveUserToken(user, jwtToken);

        return new AuthenticationResponse(jwtToken);
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .createdDate(LocalDateTime.now())
                .build();
        tokenRepository.save(token);
    }
}
