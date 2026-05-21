package com.vmarcante.time_tracker.base.infraestructure.configurations;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vmarcante.time_tracker.core.infraestructure.security.filter.SecurityInterceptor;
import com.vmarcante.time_tracker.core.infraestructure.security.ratelimit.PrivateRateLimitInterceptor;
import com.vmarcante.time_tracker.core.infraestructure.security.ratelimit.PublicRateLimitInterceptor;

import jakarta.annotation.PostConstruct;

@Configuration
public class ApplicationConfiguration implements WebMvcConfigurer {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    private final SecurityInterceptor securityInterceptor;
    private final PublicRateLimitInterceptor publicRateLimitInterceptor;
    private final PrivateRateLimitInterceptor privateRateLimitInterceptor;

    ApplicationConfiguration(
            SecurityInterceptor securityInterceptor,
            PublicRateLimitInterceptor publicRateLimitInterceptor,
            PrivateRateLimitInterceptor privateRateLimitInterceptor) {
        this.securityInterceptor = securityInterceptor;
        this.publicRateLimitInterceptor = publicRateLimitInterceptor;
        this.privateRateLimitInterceptor = privateRateLimitInterceptor;
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
    }

    // =====================================
    // ======== MVC Configurations =========

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

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Origin", "Content-Type", "Accept", "Authorization",
                        "X-Requested-With", "Access-Control-Request-Method",
                        "Access-Control-Request-Headers")
                .exposedHeaders("Authorization", "Content-Disposition", "X-Total-Count")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setDateFormatter(DateTimeFormatter.ofPattern(DATE_FORMAT));
        registrar.setDateTimeFormatter(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        registrar.registerFormatters(registry);
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .defaultContentType(MediaType.APPLICATION_JSON)
                .favorParameter(false);
    }

    // =====================================
    // ========= Application Beans =========

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:messages");
        source.setDefaultEncoding("UTF-8");
        source.setDefaultLocale(Locale.forLanguageTag("pt-BR"));
        source.setFallbackToSystemLocale(false);
        return source;
    }
}
