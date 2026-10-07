package com.vmarcante.time_tracker.core.interfaces.user.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;
import com.vmarcante.time_tracker.base.interfaces.controllers.BaseResponseController;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.RefreshTokenInputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.RequestPasswordResetInputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.ResetPasswordInputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.UserLoginInputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.dto.output.AuthenticationOutputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.in.AuthenticateUserUseCase;
import com.vmarcante.time_tracker.core.application.user.auth.in.LogoutUserUseCase;
import com.vmarcante.time_tracker.core.application.user.auth.in.RefreshTokenUseCase;
import com.vmarcante.time_tracker.core.application.user.auth.in.RequestPasswordResetUseCase;
import com.vmarcante.time_tracker.core.application.user.auth.in.ResetPasswordUseCase;
import com.vmarcante.time_tracker.core.infraestructure.security.ratelimit.annotation.RateLimit;
import com.vmarcante.time_tracker.core.shared.utils.HttpRequestUtils;
import com.vmarcante.time_tracker.core.shared.utils.UserAgentUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/public/auth")
@Tag(name = "Authentication", description = "Authentication and Register endpoints")
public class UserAuthController extends BaseResponseController {

        private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

        private final AuthenticateUserUseCase authenticateUserUseCase;
        private final LogoutUserUseCase logoutUserUseCase;
        private final RefreshTokenUseCase refreshTokenUseCase;
        private final RequestPasswordResetUseCase requestPasswordResetUseCase;
        private final ResetPasswordUseCase resetPasswordUseCase;

        @Value("${app.cookie.secure:true}")
        private boolean cookieSecure;

        @Value("${jwt.refresh-token.expiration:604800000}")
        private long refreshTokenExpirationMs;

        @Value("${server.servlet.context-path:}")
        private String contextPath;

        public UserAuthController(
                        AuthenticateUserUseCase authenticateUserUseCase,
                        LogoutUserUseCase logoutUserUseCase,
                        RefreshTokenUseCase refreshTokenUseCase,
                        RequestPasswordResetUseCase requestPasswordResetUseCase,
                        ResetPasswordUseCase resetPasswordUseCase) {
                this.authenticateUserUseCase = authenticateUserUseCase;
                this.logoutUserUseCase = logoutUserUseCase;
                this.refreshTokenUseCase = refreshTokenUseCase;
                this.requestPasswordResetUseCase = requestPasswordResetUseCase;
                this.resetPasswordUseCase = resetPasswordUseCase;
        }

        @PostMapping
        @RateLimit(maxRequests = 10, windowSeconds = 60, key = "auth:login")
        @Operation(summary = "Authenticate user", description = "Authenticates user and returns the access token; refresh token is delivered via HttpOnly cookie")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User authenticated successfully"),
                        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                        @ApiResponse(responseCode = "403", description = "User not confirmed - check email"),
                        @ApiResponse(responseCode = "429", description = "Too many requests")
        })
        public ResponseEntity<ApiResponseDTO<AuthenticationOutputDTO>> login(
                        @RequestBody UserLoginInputDTO input,
                        @RequestHeader(value = "User-Agent", required = false) String userAgent,
                        HttpServletRequest request,
                        HttpServletResponse httpResponse) throws ApplicationException {

                String ipAddress = HttpRequestUtils.getClientIpAddress(request);
                String deviceInfo = UserAgentUtils.extractDeviceInfo(userAgent);

                AuthenticationOutputDTO response = authenticateUserUseCase.execute(input, deviceInfo, ipAddress,
                                userAgent);

                HttpRequestUtils.setHttpOnlyCookie(httpResponse, REFRESH_TOKEN_COOKIE, response.refreshToken(),
                                contextPath + "/public/auth", refreshTokenExpirationMs, cookieSecure);

                return ok(response);
        }

        @PostMapping("/refresh")
        @RateLimit(maxRequests = 20, windowSeconds = 60, key = "auth:refresh")
        @Operation(summary = "Refresh access token", description = "Generates a new access token using the refresh token from HttpOnly cookie")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
                        @ApiResponse(responseCode = "400", description = "Missing refresh token cookie"),
                        @ApiResponse(responseCode = "401", description = "Expired or invalid token"),
                        @ApiResponse(responseCode = "403", description = "User not confirmed"),
                        @ApiResponse(responseCode = "429", description = "Too many requests")
        })
        public ResponseEntity<ApiResponseDTO<AuthenticationOutputDTO>> refreshToken(
                        @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshTokenCookie,
                        HttpServletResponse httpResponse) throws ApplicationException {

                if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
                        throw new ApplicationException("user.refresh.token.required", null, HttpStatus.BAD_REQUEST);
                }

                RefreshTokenInputDTO input = new RefreshTokenInputDTO(refreshTokenCookie);
                AuthenticationOutputDTO result = refreshTokenUseCase.execute(input);

                HttpRequestUtils.setHttpOnlyCookie(httpResponse, REFRESH_TOKEN_COOKIE, result.refreshToken(),
                                contextPath + "/public/auth", refreshTokenExpirationMs, cookieSecure);

                return ok(result);
        }

        @PostMapping("/request-reset")
        @RateLimit(maxRequests = 3, windowSeconds = 3600, key = "auth:request-reset")
        @Operation(summary = "Request password reset", description = "Sends a password reset email if username and email match an active user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Request processed (always returns success for security)"),
                        @ApiResponse(responseCode = "429", description = "Too many requests")
        })
        public ResponseEntity<ApiResponseDTO<Void>> requestPasswordReset(
                        @RequestBody RequestPasswordResetInputDTO input,
                        @RequestHeader(value = "Accept-Language", required = false, defaultValue = "pt") String acceptLanguage) {

                String locale = acceptLanguage != null && acceptLanguage.startsWith("en") ? "en" : "pt";
                RequestPasswordResetInputDTO inputWithLocale = new RequestPasswordResetInputDTO(
                                input.username(),
                                input.email(),
                                locale);

                requestPasswordResetUseCase.execute(inputWithLocale);
                return noContent();
        }

        @PatchMapping("/password")
        @RateLimit(maxRequests = 5, windowSeconds = 3600, key = "auth:reset-password")
        @Operation(summary = "Reset password", description = "Resets user password using the token received via email")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Password reset successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid token or password does not meet requirements"),
                        @ApiResponse(responseCode = "429", description = "Too many requests")
        })
        public ResponseEntity<ApiResponseDTO<Void>> resetPassword(@RequestBody ResetPasswordInputDTO input)
                        throws ApplicationException {
                resetPasswordUseCase.execute(input);
                return noContent();
        }

        @DeleteMapping("/session")
        @RateLimit(maxRequests = 10, windowSeconds = 60, key = "auth:logout")
        @Operation(summary = "Logout user", description = "Logs out the current user, invalidates all sessions and clears the refresh token cookie")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "User logged out successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "429", description = "Too many requests")
        })
        public ResponseEntity<ApiResponseDTO<Void>> logout(HttpServletResponse httpResponse) throws ApplicationException {
                logoutUserUseCase.execute();
                HttpRequestUtils.expireCookie(httpResponse, REFRESH_TOKEN_COOKIE,
                                contextPath + "/public/auth", cookieSecure);
                return noContent();
        }

}
