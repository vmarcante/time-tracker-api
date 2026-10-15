package com.vmarcante.time_tracker.core.application.user.auth.dto.output;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record AuthenticationOutputDTO(
        String accessToken,
        // Refresh token é entregue exclusivamente via HttpOnly cookie, nunca serializado no body
        @JsonIgnore String refreshToken,
        Long accessTokenExpiresIn,
        Long refreshTokenExpiresIn,
        String tokenType) {

    public AuthenticationOutputDTO(String accessToken, String refreshToken, Long accessTokenExpiresIn,
            Long refreshTokenExpiresIn) {
        this(accessToken, refreshToken, accessTokenExpiresIn, refreshTokenExpiresIn, "Bearer");
    }
}
