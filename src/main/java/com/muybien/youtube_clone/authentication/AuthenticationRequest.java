package com.muybien.youtube_clone.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AuthenticationRequest(
        @Email(message = "Email should be valid.")
        @NotEmpty(message = "Email is mandatory.")
        @NotNull(message = "Email is mandatory.")
        String email,
        @NotEmpty(message = "Password is mandatory.")
        @NotNull(message = "Password is mandatory.")
        String password
) {
}
