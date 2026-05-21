package com.vmarcante.time_tracker.core.infraestructure.security.ratelimit;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.vmarcante.time_tracker.core.infraestructure.security.ratelimit.annotation.RateLimit;
import com.vmarcante.time_tracker.core.infraestructure.security.ratelimit.exception.RateLimitExceededException;
import com.vmarcante.time_tracker.core.shared.utils.HttpRequestUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PublicRateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    private static final int DEFAULT_PUBLIC_MAX_REQUESTS = 50;
    private static final int DEFAULT_WINDOW_SECONDS = 60;

    public PublicRateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

        String clientIp = HttpRequestUtils.getClientIpAddress(request);
        String key;
        int maxRequests;
        int windowSeconds;

        if (rateLimit == null) {
            // Rate limit padrão para endpoints públicos (por IP)
            key = "public:ip:" + clientIp;
            maxRequests = DEFAULT_PUBLIC_MAX_REQUESTS;
            windowSeconds = DEFAULT_WINDOW_SECONDS;
        } else {
            // Rate limit customizado via anotação (se aplicável a endpoints públicos)
            key = buildRateLimitKey(rateLimit, clientIp, request);
            maxRequests = rateLimit.maxRequests();
            windowSeconds = rateLimit.windowSeconds();

            // Check progressive mode
            if (rateLimit.progressive()) {
                return handleProgressiveRateLimit(request, response, key, rateLimit.progressiveLevels());
            }
        }

        boolean allowed = rateLimitService.isAllowed(key, maxRequests, windowSeconds);

        long remaining = rateLimitService.getRemainingRequests(key, maxRequests);
        response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));

        if (!allowed) {
            long resetTime = rateLimitService.getResetTime(key);
            response.setHeader("X-RateLimit-Reset", String.valueOf(resetTime));
            response.setHeader("Retry-After", String.valueOf(resetTime));

            String requestId = request.getHeader("X-Request-ID");
            log.warn("[Public Rate Limit] Request blocked | IP: {} | Path: {} | Method: {} | Retry after: {}s",
                    clientIp, request.getRequestURI(), request.getMethod(), resetTime, requestId);

            throw new RateLimitExceededException(resetTime);
        }

        return true;
    }

    private boolean handleProgressiveRateLimit(HttpServletRequest request, HttpServletResponse response,
            String key, ProgressiveLevel[] levels) throws Exception {
        ProgressiveRateLimitResult result = rateLimitService.isAllowedProgressive(key, levels);

        response.setHeader("X-RateLimit-Progressive", "true");
        response.setHeader("X-RateLimit-Level", result.getLevel());
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.getRemainingRequests()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(result.getResetTime()));

        if (!result.isAllowed()) {
            response.setHeader("Retry-After", String.valueOf(result.getResetTime()));

            String requestId = request.getHeader("X-Request-ID");
            String clientIp = HttpRequestUtils.getClientIpAddress(request);
            log.warn("[Public Rate Limit] Request blocked (progressive) | IP: {} | Path: {} | Method: {} | Level: {} | Retry after: {}s",
                    clientIp, request.getRequestURI(), request.getMethod(), result.getLevel(), result.getResetTime(), requestId);

            throw new RateLimitExceededException(result.getResetTime());
        }

        return true;
    }

    private String buildRateLimitKey(RateLimit rateLimit, String clientIp, HttpServletRequest request) {
        if (!rateLimit.key().isEmpty()) {
            return rateLimit.key() + ":" + clientIp;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();
        return String.format("%s:%s:%s", method, path, clientIp);
    }
}
