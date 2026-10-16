package com.vmarcante.time_tracker.core.infrastructure.security.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.vmarcante.time_tracker.core.infrastructure.security.filter.SecurityInterceptor;
import com.vmarcante.time_tracker.core.infrastructure.security.ratelimit.PrivateRateLimitInterceptor;
import com.vmarcante.time_tracker.core.infrastructure.security.ratelimit.PublicRateLimitInterceptor;

@Configuration
public class SecurityWebMvcConfiguration implements WebMvcConfigurer {

    private final SecurityInterceptor securityInterceptor;
    private final PublicRateLimitInterceptor publicRateLimitInterceptor;
    private final PrivateRateLimitInterceptor privateRateLimitInterceptor;

    SecurityWebMvcConfiguration(
            SecurityInterceptor securityInterceptor,
            PublicRateLimitInterceptor publicRateLimitInterceptor,
            PrivateRateLimitInterceptor privateRateLimitInterceptor) {
        this.securityInterceptor = securityInterceptor;
        this.publicRateLimitInterceptor = publicRateLimitInterceptor;
        this.privateRateLimitInterceptor = privateRateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Rate limit público por IP (antes do SecurityInterceptor)
        registry.addInterceptor(publicRateLimitInterceptor)
                .addPathPatterns("/public/**")
                .excludePathPatterns("/error")
                .order(1);

        // SecurityInterceptor (validação JWT e @AuthSecure)
        registry.addInterceptor(securityInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/public/**", "/error")
                .order(2);

        // Rate limit privado por userId (depois do SecurityInterceptor)
        registry.addInterceptor(privateRateLimitInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/public/**", "/error")
                .order(3);
    }
}
