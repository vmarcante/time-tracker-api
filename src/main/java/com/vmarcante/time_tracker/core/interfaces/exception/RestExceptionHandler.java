package com.vmarcante.time_tracker.core.interfaces.exception;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.vmarcante.time_tracker.base.domain.exception.ApiErrorResponse;
import com.vmarcante.time_tracker.base.domain.exception.BaseException;
import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.exception.DomainException;
import com.vmarcante.time_tracker.core.domain.translation.service.TranslationService;
import com.vmarcante.time_tracker.core.infraestructure.exception.InfraestructureException;
import com.vmarcante.time_tracker.core.infraestructure.security.ratelimit.exception.RateLimitExceededException;
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
    private final HttpStatus infraestructureDefaultStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    private final HttpStatus applicationDefaultStatus = HttpStatus.BAD_REQUEST;
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

    @ExceptionHandler(InfraestructureException.class)
    public ResponseEntity<ApiResponseDTO<ApiErrorResponse>> handleInfraestructureException(InfraestructureException ex,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        logException(ex, "Infraestructure", requestId);
        return buildErrorResponse(ex, infraestructureDefaultStatus, request);
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponseDTO<ApiErrorResponse>> handleApplicationException(ApplicationException ex,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        logException(ex, "Application", requestId);
        return buildErrorResponse(ex, applicationDefaultStatus, request);
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

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResponseDTO<ApiErrorResponse>> handleIllegalArgumentException(IllegalArgumentException ex,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        logException(ex, requestId);
        String key = "error.internal.server";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = getMessage(key);
        ApiErrorResponse errorResponse = new ApiErrorResponse(key, message);
        ApiResponseDTO<ApiErrorResponse> response = ApiResponseDTO.error(status.value(), errorResponse);

        return ResponseEntity
                .status(status)
                .header("X-Error-Code", key)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<ApiErrorResponse>> handleGenericException(Exception ex,
            HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        logException(ex, requestId);

        String key = "error.internal.server";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        String message = getMessage(key);
        ApiErrorResponse errorResponse = new ApiErrorResponse(key, message);
        ApiResponseDTO<ApiErrorResponse> response = ApiResponseDTO.error(status.value(), errorResponse);

        return ResponseEntity
                .status(status)
                .header("X-Error-Code", key)
                .body(response);
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

    private void logException(Exception ex, String requestId) {
        String logMessage = "[Exception Handler] Unhandled exception: message={}";

        if (isProduction()) {
            log.error(logMessage, requestId, ex.getMessage());
        } else {
            log.error(logMessage, requestId, ex.getMessage(), ex);
        }
    }

    private String getMessage(String key, Object... arguments) {
        return translationService.translate(key, arguments);
    }

    private boolean isProduction() {
        return environment.matchesProfiles("prod");
    }

    private void logException(BaseException ex, String violationType, String requestId) {
        String exceptionName = ex.getClass().getSimpleName();
        String logMessage;

        switch (exceptionName) {
            case "DomainException":
                logMessage = "[Exception Handler] Domain Violation: key={}";
                break;
            case "InfraestructureException":
                logMessage = "[Exception Handler] Infraestructure Violation: key={}";
                break;
            case "ApplicationException":
                logMessage = "[Exception Handler] Application Violation: key={}";
                break;
            case "VoException":
                logMessage = "[Exception Handler] Value Object Violation: key={}";
                break;
            default:
                logMessage = "[Exception Handler] Unhandled exception: key={}";
                break;
        }

        if (isProduction()) {
            log.warn(logMessage, ex.getExceptionKey(), requestId);
            return;
        }

        log.warn(logMessage, ex.getExceptionKey(), requestId, ex);
    }
}
