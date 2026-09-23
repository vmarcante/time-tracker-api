package com.vmarcante.time_tracker.core.infraestructure.company.event.listener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.company.event.CompanyInvitationCreatedEvent;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.email.model.EmailData;
import com.vmarcante.time_tracker.core.domain.email.service.EmailService;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.shared.utils.StringValidationUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CompanyInvitationCreatedEventListener {

    private final EmailService emailService;
    private final PersonRepository personRepository;
    private final CompanyRepository companyRepository;
    private final String frontendUrl;

    public CompanyInvitationCreatedEventListener(
            EmailService emailService,
            PersonRepository personRepository,
            CompanyRepository companyRepository,
            @Value("${app.frontend.url:https://app.timetracker.com}") String frontendUrl) {
        this.emailService = emailService;
        this.personRepository = personRepository;
        this.companyRepository = companyRepository;
        this.frontendUrl = frontendUrl;
    }

    @Async
    @EventListener
    public void handleCompanyInvitationCreated(CompanyInvitationCreatedEvent event) {
        try {
            Person invitee = personRepository.findById(event.getInviteeUserId()).orElse(null);
            if (invitee == null || invitee.getEmail() == null
                    || !StringValidationUtils.containsContent(invitee.getEmail().address())) {
                log.warn("[Company Invitation Event] Invitee person not found or without email | User: {}",
                        event.getInviteeUserId());
                return;
            }

            String companyName = companyRepository.findById(event.getCompanyId())
                    .map(c -> c.getLegalName())
                    .orElse("");
            String inviterName = personRepository.findNameById(event.getInviterUserId()).orElse("");
            String role = companyRepository.findMembershipById(event.getMembershipId())
                    .map(m -> m.getRole().name())
                    .orElse("");

            Map<String, Object> templateData = new HashMap<>();
            templateData.put("name", invitee.getName());
            templateData.put("companyName", companyName);
            templateData.put("inviterName", inviterName);
            templateData.put("role", role);
            templateData.put("invitationsLink", frontendUrl + "/invitations");

            boolean isPortuguese = "pt".equalsIgnoreCase(invitee.getLocale());
            String subject = isPortuguese
                    ? "Você foi convidado para " + companyName + " - Time Tracker"
                    : "You were invited to " + companyName + " - Time Tracker";
            Locale locale = isPortuguese
                    ? Locale.forLanguageTag("pt-BR")
                    : Locale.forLanguageTag("en-US");

            emailService.sendEmail(new EmailData(
                    invitee.getEmail().address(), subject, "company_invitation", templateData, locale));

            log.info("[Company Invitation Event] Invitation email sent | User: {} | Company: {}",
                    event.getInviteeUserId(), event.getCompanyId());
        } catch (Exception e) {
            log.error("[Company Invitation Event] Failed to send invitation email | User: {}",
                    event.getInviteeUserId(), e);
        }
    }
}
