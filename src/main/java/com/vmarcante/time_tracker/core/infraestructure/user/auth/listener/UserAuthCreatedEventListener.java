package com.vmarcante.time_tracker.core.infraestructure.user.auth.listener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.email.model.EmailData;
import com.vmarcante.time_tracker.core.domain.email.service.EmailService;
import com.vmarcante.time_tracker.core.domain.user.auth.event.UserAuthCreatedEvent;
import com.vmarcante.time_tracker.core.shared.utils.StringValidationUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UserAuthCreatedEventListener {

    private final EmailService emailService;
    private final String frontendUrl;

    public UserAuthCreatedEventListener(
            EmailService emailService,
            @Value("${app.frontend.url:https://app.timetracker.com}") String frontendUrl) {
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
    }

    @Async
    @EventListener
    public void handleUserAuthCreated(UserAuthCreatedEvent event) {

        try {

            log.info("[Event Listener] - Dispatching User Auth email event | User: {} | Email: {}",
                    event.getPerson().getName(), event.getPerson().getEmail().address());

            if (!StringValidationUtils.containsContent(event.getPerson().getEmail().address())) {
                log.warn("[Event Listener] - Email address is null or empty, skipping email send");
                return;
            }

            String email = event.getPerson().getEmail().address();

            log.debug("[Event Listener] - Sending welcome email to: {}", email);
            log.debug("[Event Listener] - User ID: {}",
                    event.getPerson().getId());

            EmailData emailData = buildWelcomeEmailData(event);

            emailService.sendEmail(emailData);

            log.info("[Event Listener] - User Auth email event dispatched | User: {} | Email: {}",
                    event.getPerson().getName(), event.getPerson().getEmail().address());

        } catch (Exception e) {
            log.error("[Event Listener] - Failed to send welcome email to: {}",
                    event.getPerson().getEmail().address(), e);
        }
    }

    private EmailData buildWelcomeEmailData(UserAuthCreatedEvent event) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("nomeUsuario", event.getPerson().getName());
        templateData.put("confirmationLink", buildConfirmationLink(event.getAccessToken()));

        boolean isPortuguese = "pt".equalsIgnoreCase(event.getLocale());
        String subject = isPortuguese
                ? "Bem-vindo ao Time Tracker!"
                : "Welcome to Time Tracker!";

        Locale locale = isPortuguese
                ? Locale.forLanguageTag("pt-BR")
                : Locale.forLanguageTag("en-US");

        return new EmailData(
                event.getPerson().getEmail().address(),
                subject,
                "welcome_user",
                templateData,
                locale);
    }

    private String buildConfirmationLink(String token) {
        return frontendUrl + "/confirm-registration/" + token;
    }
}
