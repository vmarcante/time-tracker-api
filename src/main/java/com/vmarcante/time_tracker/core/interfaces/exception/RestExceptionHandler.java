package com.vmarcante.time_tracker.core.interfaces.exception;

import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.vmarcante.time_tracker.base.domain.exception.ApiErrorResponse;
import com.vmarcante.time_tracker.base.domain.exception.BaseException;
import com.vmarcante.time_tracker.base.domain.exception.InfrastructureException;
import com.vmarcante.time_tracker.base.domain.exception.RateLimitExceededException;
import com.vmarcante.time_tracker.base.domain.exception.SecurityException;
import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.exception.DomainException;
import com.vmarcante.time_tracker.core.domain.translation.service.TranslationService;
import com.vmarcante.time_tracker.core.shared.vo.exception.VoException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice(basePackages = "com.vmarcante.time_tracker.core.interfaces")
@Slf4j
public class RestExceptionHandler {

    private final TranslationService translationService;
    private final Environment environment;
    private final HttpStatus domainDefaultStatus = HttpStatus.UNPROCESSABLE_CONTENT;
    private final HttpStatus infrastructureDefaultStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    private final HttpStatus applicationDefaultStatus = HttpStatus.BAD_REQUEST;
    private final HttpStatus securityDefaultStatus = HttpStatus.UNAUTHORIZED;
    private final HttpStatus voDefaultStatus = HttpStatus.BAD_REQUEST;

    public RestExceptionHandler(TranslationService translationService, Environment environment) {
        this.translationService = translationService;
        this.environment = environment;
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponseDTO<ApiErrorResponse>> handleDomainException(DomainException ex,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        logException(ex, "Domain", requestId);
        return buildErrorResponse(ex, domainDefaultStatus, request);
    }

    @ExceptionHandler(InfrastructureException.class)
    public ResponseEntity<ApiResponseDTO<ApiErrorResponse>> handleInfrastructureException(InfrastructureException ex,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        logException(ex, "Infrastructure", requestId);
        return buildErrorResponse(ex, infrastructureDefaultStatus, request);
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponseDTO<ApiErrorResponse>> handleApplicationException(ApplicationException ex,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        logException(ex, "Application", requestId);
        return buildErrorResponse(ex, applicationDefaultStatus, request);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponseDTO<ApiErrorResponse>> handleSecurityException(SecurityException ex,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        logException(ex, "Security", requestId);
        return buildErrorResponse(ex, securityDefaultStatus, request);
    }

    @ExceptionHandler(VoException.class)
    public ResponseEntity<ApiResponseDTO<ApiErrorResponse>> handleVo(VoException ex,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        logException(ex, "Value Object", requestId);
        return buildErrorResponse(ex, voDefaultStatus, request);
    }

    // RateLimitInterceptor already logs, no need to log again
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponseDTO<ApiErrorResponse>> handleRateLimitExceeded(
            RateLimitExceededException ex,
            HttpServletRequest request,
            HttpServletResponse response) {

        response.setHeader("Retry-After", String.valueOf(ex.getRetryAfter()));

        return buildErrorResponse(ex, HttpStatus.TOO_MANY_REQUESTS, request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponseDTO<ApiErrorResponse>> handleNoResourceFound(NoResourceFoundException ex,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        logException(ex, requestId);
        return buildSimpleErrorResponse("error.not.found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponseDTO<ApiErrorResponse>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        logException(ex, requestId);
        return buildSimpleErrorResponse("error.method.not.allowed", HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler({ MissingServletRequestParameterException.class, BindException.class })
    public ResponseEntity<ApiResponseDTO<ApiErrorResponse>> handleBindingErrors(Exception ex,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        logException(ex, requestId);
        return buildSimpleErrorResponse("error.bad.request", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseDTO<ApiErrorResponse>> handleDataIntegrity(DataIntegrityViolationException ex,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        logException(ex, requestId);
        return buildSimpleErrorResponse("error.conflict", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDTO<ApiErrorResponse>> handleAccessDenied(AccessDeniedException ex,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        logException(ex, requestId);
        return buildSimpleErrorResponse("error.access.denied", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({ IllegalArgumentException.class, MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class })
    public ResponseEntity<ApiResponseDTO<ApiErrorResponse>> handleIllegalArgumentException(IllegalArgumentException ex,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        logException(ex, requestId);
        return buildSimpleErrorResponse("error.bad.request", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<ApiErrorResponse>> handleGenericException(Exception ex,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        logException(ex, requestId);
        return buildSimpleErrorResponse("error.internal.server", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponseDTO<ApiErrorResponse>> buildErrorResponse(BaseException ex,
            HttpStatus defaultStatus, HttpServletRequest request) {
        HttpStatus status = ex.getCustomStatus() != null ? ex.getCustomStatus() : defaultStatus;
        String message = getMessage(ex.getExceptionKey(), ex.getArguments());

        ApiErrorResponse errorResponse = new ApiErrorResponse(ex.getExceptionKey(), message);
        ApiResponseDTO<ApiErrorResponse> response = ApiResponseDTO.error(status.value(), errorResponse);

        return ResponseEntity
                .status(status)
                .header("X-Error-Code", ex.getExceptionKey())
                .body(response);
    }

    private ResponseEntity<ApiResponseDTO<ApiErrorResponse>> buildSimpleErrorResponse(String key, HttpStatus status) {
        String message = getMessage(key);
        ApiErrorResponse errorResponse = new ApiErrorResponse(key, message);
        ApiResponseDTO<ApiErrorResponse> response = ApiResponseDTO.error(status.value(), errorResponse);

        return ResponseEntity
                .status(status)
                .header("X-Error-Code", key)
                .body(response);
    }

    private void logException(Exception ex, String requestId) {
        if (isProduction()) {
            log.error("[Exception Handler] Unhandled exception: requestId={} message={}", requestId, ex.getMessage());
        } else {
            log.error("[Exception Handler] Unhandled exception: requestId={} message={}", requestId, ex.getMessage(),
                    ex);
        }
    }

    private String getMessage(String key, Object... arguments) {
        return translationService.translate(key, arguments);
    }

    private boolean isProduction() {
        return environment.matchesProfiles("prod");
    }

    private void logException(BaseException ex, String violationType, String requestId) {
        if (isProduction()) {
            log.warn("[Exception Handler] {} Violation: key={} requestId={}", violationType, ex.getExceptionKey(),
                    requestId);
            return;
        }

        log.warn("[Exception Handler] {} Violation: key={} requestId={}", violationType, ex.getExceptionKey(),
                requestId, ex);
    }
}
