package com.muybien.youtube_clone.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RegisterRequest(

        @NotEmpty(message = "Firstname is mandatory.")
        @NotNull(message = "Firstname is mandatory.")
        String firstname,
        @NotEmpty(message = "Lastname is mandatory.")
        @NotNull(message = "Lastname is mandatory.")
        String lastname,
        @Email(message = "Email should be valid.")
        @NotEmpty(message = "Email is mandatory.")
        @NotNull(message = "Email is mandatory.")
        String email,
        @NotEmpty(message = "Password is mandatory.")
        @NotNull(message = "Password is mandatory.")
        String password
) {
}