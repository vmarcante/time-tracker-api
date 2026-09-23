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
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.event.PasswordChangedEvent;
import com.vmarcante.time_tracker.core.shared.utils.StringValidationUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PasswordChangedEventListener {

    private final EmailService emailService;
    private final PersonRepository personRepository;
    private final String frontendUrl;

    public PasswordChangedEventListener(
            EmailService emailService,
            PersonRepository personRepository,
            @Value("${app.frontend.url:https://app.timetracker.com}") String frontendUrl) {
        this.emailService = emailService;
        this.personRepository = personRepository;
        this.frontendUrl = frontendUrl;
    }

    @Async
    @EventListener
    public void handlePasswordChanged(PasswordChangedEvent event) {
        try {
            Person person = personRepository.findById(event.getUserId()).orElse(null);
            if (person == null || person.getEmail() == null
                    || !StringValidationUtils.containsContent(person.getEmail().address())) {
                log.warn("[Password Changed Event] Person not found or without email | User: {}",
                        event.getUserId());
                return;
            }

            Map<String, Object> templateData = new HashMap<>();
            templateData.put("name", person.getName());
            templateData.put("resetLink", frontendUrl + "/forgot-password");

            boolean isPortuguese = "pt".equalsIgnoreCase(person.getLocale());
            String subject = isPortuguese
                    ? "Sua senha foi alterada - Time Tracker"
                    : "Your password was changed - Time Tracker";
            Locale locale = isPortuguese
                    ? Locale.forLanguageTag("pt-BR")
                    : Locale.forLanguageTag("en-US");

            emailService.sendEmail(new EmailData(
                    person.getEmail().address(), subject, "password_changed", templateData, locale));

            log.info("[Password Changed Event] Confirmation email sent | User: {}", event.getUserId());
        } catch (Exception e) {
            log.error("[Password Changed Event] Failed to send confirmation email | User: {}",
                    event.getUserId(), e);
        }
    }
}
