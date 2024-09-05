package com.muybien.youtube_clone.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record AuthenticationResponse(
        @JsonProperty("token")
        String token
) {
}