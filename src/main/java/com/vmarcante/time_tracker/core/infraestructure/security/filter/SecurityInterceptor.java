package com.vmarcante.time_tracker.core.infraestructure.security.filter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.user.auth.annotation.AuthSecure;
import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;
import com.vmarcante.time_tracker.core.domain.user.auth.model.UserContextData;
import com.vmarcante.time_tracker.core.domain.user.auth.port.JwtPort;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;
import com.vmarcante.time_tracker.core.domain.user.auth.repository.UserAuthRepository;
import com.vmarcante.time_tracker.core.domain.user.enums.AffiliationStatus;
import com.vmarcante.time_tracker.core.domain.user.enums.UserRoleType;
import com.vmarcante.time_tracker.core.shared.utils.HttpRequestUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SecurityInterceptor implements HandlerInterceptor {

    private final JwtPort jwtPort;
    private final SecurityContextPort securityContextPort;
    private final UserAuthRepository userAuthRepository;

    @Value("${jwt.issuer:time-tracker-api}")
    private String expectedIssuer;

    @Value("${jwt.audience:time-tracker-client}")
    private String expectedAudience;

    @Value("${health.app-key:}")
    private String expectedAppKey;

    public SecurityInterceptor(
            JwtPort jwtPort,
            SecurityContextPort securityContextPort,
            UserAuthRepository userAuthRepository) {
        this.jwtPort = jwtPort;
        this.securityContextPort = securityContextPort;
        this.userAuthRepository = userAuthRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        AuthSecure authSecure = method.getAnnotation(AuthSecure.class);
        String requestId = request.getHeader("X-Request-ID");

        if (authSecure == null) {
            log.trace("[SecurityInterceptor] Endpoint {} has no @AuthSecure, skipping auth", 
                request.getRequestURI(), requestId);
            return true;
        }

        log.debug("[SecurityInterceptor] Endpoint {} requires authentication", 
            request.getRequestURI(), requestId);

        if (authSecure.appKeyAllowed() && isValidAppKey(request)) {
            log.debug("[SecurityInterceptor] Endpoint {} authenticated via AppKey", 
                request.getRequestURI(), requestId);
            return true;
        }

        String token = HttpRequestUtils.extractToken(request);

        if (token == null) {
            log.warn("[SecurityInterceptor] Missing Authorization header | URI={}", 
                request.getRequestURI(), requestId);
            throw new ApplicationException("user.session.invalid", null, HttpStatus.UNAUTHORIZED);
        }

        if (!jwtPort.validateToken(token)) {
            log.warn("[SecurityInterceptor] Invalid token | URI={}", 
                request.getRequestURI(), requestId);
            throw new ApplicationException("user.session.invalid", null, HttpStatus.UNAUTHORIZED);
        }

        String tokenType = jwtPort.extractTokenType(token);
        if (!"access".equals(tokenType)) {
            log.warn("[SecurityInterceptor] Invalid token type: {} | URI={}", 
                tokenType, request.getRequestURI(), requestId);
            throw new ApplicationException("user.session.invalid", null, HttpStatus.UNAUTHORIZED);
        }

        // Validate issuer
        String tokenIssuer = jwtPort.extractIssuer(token);
        if (!expectedIssuer.equals(tokenIssuer)) {
            log.warn("[SecurityInterceptor] Invalid issuer: {} | Expected: {} | URI={}", 
                tokenIssuer, expectedIssuer, request.getRequestURI(), requestId);
            throw new ApplicationException("user.session.invalid", null, HttpStatus.UNAUTHORIZED);
        }

        // Validate audience
        String tokenAudience = jwtPort.extractAudience(token);
        if (!expectedAudience.equals(tokenAudience)) {
            log.warn("[SecurityInterceptor] Invalid audience: {} | Expected: {} | URI={}", 
                tokenAudience, expectedAudience, request.getRequestURI(), requestId);
            throw new ApplicationException("user.session.invalid", null, HttpStatus.UNAUTHORIZED);
        }

        UUID userId = jwtPort.extractUserId(token);

        Optional<UserAuth> userAuth = userAuthRepository.findById(userId);
        if (userAuth.isEmpty()) {
            log.warn("[SecurityInterceptor] User not found for token | URI={}", 
                request.getRequestURI(), requestId);
            throw new ApplicationException("user.session.invalid", null, HttpStatus.UNAUTHORIZED);
        }

        UserAuth user = userAuth.get();

        if (!user.getUserConfirmed()) {
            log.warn("[SecurityInterceptor] User not confirmed | User: {} | URI={}", 
                user.getUsername(), request.getRequestURI(), requestId);
            throw new ApplicationException("user.session.invalid", null, HttpStatus.UNAUTHORIZED);
        }

        if (!user.getActive()) {
            log.warn("[SecurityInterceptor] User not active | User: {} | URI={}", 
                user.getUsername(), request.getRequestURI(), requestId);
            throw new ApplicationException("user.session.invalid", null, HttpStatus.UNAUTHORIZED);
        }

        if (user.getAffiliation() == AffiliationStatus.PENDING && !authSecure.allowPendingOnboarding()) {
            log.warn("[SecurityInterceptor] Onboarding pending | User: {} | URI={}",
                user.getUsername(), request.getRequestURI(), requestId);
            throw new ApplicationException("user.onboarding.pending", null, HttpStatus.FORBIDDEN);
        }

        if (authSecure.acceptedRoles() != null && authSecure.acceptedRoles().length > 0) {
            int maxRoleLeve = Arrays.stream(authSecure.acceptedRoles()).mapToInt(UserRoleType::getLevel).max().orElse(0);
            int currentRoleLevel = user.getRole().getLevel();

            if (currentRoleLevel < maxRoleLeve) {
                log.warn("[SecurityInterceptor] User role not accepted | User: {} | URI={}", 
                    user.getUsername(), request.getRequestURI(), requestId);
                throw new ApplicationException("user.session.invalid", null, HttpStatus.UNAUTHORIZED);
            }
        }

        String username = jwtPort.extractUsername(token);
        Integer seqId = jwtPort.extractSeqId(token);
        String localeStr = jwtPort.extractLocale(token);
        
        Locale userLocale = localeStr != null ? Locale.forLanguageTag(localeStr) : Locale.forLanguageTag("pt");

        LocaleContextHolder.setLocale(userLocale);

        UserContextData userContext = new UserContextData(
                userId,
                seqId,
                username,
                token,
                user.getRole(),
                userLocale);

        securityContextPort.setCurrentUser(userContext);

        log.debug("[SecurityInterceptor] User {} authenticated for {}", 
            username, request.getRequestURI(), requestId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) throws Exception {
        securityContextPort.clear();
        LocaleContextHolder.resetLocaleContext();
    }

    private boolean isValidAppKey(HttpServletRequest request) {
        if (expectedAppKey == null || expectedAppKey.isBlank()) {
            return false;
        }
        String appKey = request.getHeader("X-App-Key");
        return appKey != null && appKey.equals(expectedAppKey);
    }
}
