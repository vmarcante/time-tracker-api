package com.vmarcante.time_tracker.core.application.user.auth.dto.output;

public record AuthenticationOutputDTO(
        String accessToken,
        String refreshToken,
        Long accessTokenExpiresIn,
        Long refreshTokenExpiresIn,
        String tokenType) {

    public AuthenticationOutputDTO(String accessToken, String refreshToken, Long accessTokenExpiresIn,
            Long refreshTokenExpiresIn) {
        this(accessToken, refreshToken, accessTokenExpiresIn, refreshTokenExpiresIn, "Bearer");
    }
}
