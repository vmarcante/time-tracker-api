package com.vmarcante.time_tracker.core.infraestructure.user.auth.event.listener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.email.model.EmailData;
import com.vmarcante.time_tracker.core.domain.email.service.EmailService;
import com.vmarcante.time_tracker.core.domain.user.auth.event.PasswordResetRequestedEvent;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PasswordResetRequestedEventListener {

    private final EmailService emailService;
    private final String frontendUrl;
    private final int resetTokenExpirationMinutes;

    public PasswordResetRequestedEventListener(
            EmailService emailService,
            @Value("${app.frontend.url:https://app.timetracker.com}") String frontendUrl,
            @Value("${app.reset-token.expiration-minutes:15}") int resetTokenExpirationMinutes) {
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
        this.resetTokenExpirationMinutes = resetTokenExpirationMinutes;
    }

    @Async
    @EventListener
    public void handlePasswordResetRequested(PasswordResetRequestedEvent event) {
        try {
            log.info("[Password Reset Event] Email event dispatched | User: {} | Email: {}",
                    event.getUsername(), event.getEmail());

            EmailData emailData = buildPasswordResetEmailData(event);
            emailService.sendEmail(emailData);

            log.info("[Password Reset Event] Email sent successfully | User: {}", event.getUsername());

        } catch (Exception e) {
            log.error("[Password Reset Event] Failed to send email to: {}", event.getEmail(), e);
        }
    }

    private EmailData buildPasswordResetEmailData(PasswordResetRequestedEvent event) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("name", event.getName());
        templateData.put("resetLink", buildResetLink(event.getResetToken()));
        templateData.put("expirationMinutes", resetTokenExpirationMinutes);

        boolean isPortuguese = "pt".equalsIgnoreCase(event.getLocale());
        String subject = isPortuguese
                ? "Redefinição de Senha - Time Tracker"
                : "Password Reset - Time Tracker";

        Locale locale = isPortuguese ? Locale.forLanguageTag("pt-BR") : Locale.forLanguageTag("en-US");

        return new EmailData(
                event.getEmail(),
                subject,
                "password_reset",
                templateData,
                locale);
    }

    private String buildResetLink(String token) {
        return frontendUrl + "/reset-password/" + token;
    }
}
