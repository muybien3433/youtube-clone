package com.muybien.youtube_clone.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muybien.youtube_clone.config.JwtService;
import com.muybien.youtube_clone.token.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AuthenticationController.class)
public class AuthenticationControllerTest {

    @MockBean private AuthenticationService authenticationService;
    @MockBean private JwtService jwtService;
    @MockBean private TokenRepository tokenRepository;
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private AuthenticationResponse authenticationResponse;

    @BeforeEach
    public void setUp() {
         authenticationResponse = AuthenticationResponse.builder()
                        .token("sample-token")
                        .build();
    }

    @Test
    @WithMockUser(username = "user")
    void testRegister() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                        .firstname("John")
                        .lastname("Doe")
                        .email("john.doe@example.com")
                        .password("password")
                        .build();

        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(authenticationResponse);

        mockMvc.perform(post("/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(content().json(objectMapper.writeValueAsString(authenticationResponse)));
    }

    @Test
    @WithMockUser(username = "user")
    void testRegisterWhenInvalidCredentials() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstname("")
                .lastname("")
                .email("")
                .password("")
                .build();

        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(authenticationResponse);

        mockMvc.perform(post("/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testAuthenticate() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                        .email("john.doe@example.com")
                        .password("password")
                        .build();

        when(authenticationService.authenticate(any(AuthenticationRequest.class))).thenReturn(authenticationResponse);

        mockMvc.perform(post("/auth/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(content().json(objectMapper.writeValueAsString(authenticationResponse)));
    }

    @Test
    @WithMockUser
    void testAuthenticateWhenInvalidCredentials() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("")
                .password("")
                .build();

        when(authenticationService.authenticate(any(AuthenticationRequest.class))).thenReturn(authenticationResponse);

        mockMvc.perform(post("/auth/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
