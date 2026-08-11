package com.vmarcante.time_tracker.core.infraestructure.security.filter;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestFilter extends OncePerRequestFilter {

    private static final String X_REQUEST_ID = "X-Request-ID";
    private static final String MDC_REQUEST_ID = "requestId";
    private static final String MDC_POD_NAME = "podName";
    private static final String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    private static final String X_FRAME_OPTIONS = "X-Frame-Options";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = request.getHeader(X_REQUEST_ID);

        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        // Add to response header
        response.setHeader(X_REQUEST_ID, requestId);
        response.setHeader(X_CONTENT_TYPE_OPTIONS, "nosniff");
        response.setHeader(X_FRAME_OPTIONS, "DENY");

        // Add to MDC for logging
        MDC.put(MDC_REQUEST_ID, requestId);
        MDC.put(MDC_POD_NAME, System.getenv().getOrDefault("HOSTNAME", "unknown"));

        try {
            // Wrap request to include X-Request-ID header
            HttpServletRequest wrappedRequest = new RequestIdWrapper(request, requestId);
            filterChain.doFilter(wrappedRequest, response);
        } finally {
            MDC.remove(MDC_REQUEST_ID);
            MDC.remove(MDC_POD_NAME);
        }
    }

    private static class RequestIdWrapper extends HttpServletRequestWrapper {
        private final String requestId;

        public RequestIdWrapper(HttpServletRequest request, String requestId) {
            super(request);
            this.requestId = requestId;
        }

        @Override
        public String getHeader(String name) {
            if (X_REQUEST_ID.equalsIgnoreCase(name)) {
                return requestId;
            }
            return super.getHeader(name);
        }
    }
}
