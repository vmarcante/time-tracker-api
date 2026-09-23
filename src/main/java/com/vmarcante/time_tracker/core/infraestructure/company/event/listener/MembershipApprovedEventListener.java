package com.vmarcante.time_tracker.core.infraestructure.company.event.listener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.company.event.MembershipApprovedEvent;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.email.model.EmailData;
import com.vmarcante.time_tracker.core.domain.email.service.EmailService;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.shared.utils.StringValidationUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MembershipApprovedEventListener {

    private final EmailService emailService;
    private final PersonRepository personRepository;
    private final CompanyRepository companyRepository;
    private final String frontendUrl;

    public MembershipApprovedEventListener(
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
    public void handleMembershipApproved(MembershipApprovedEvent event) {
        try {
            Person member = personRepository.findById(event.getMemberUserId()).orElse(null);
            if (member == null || member.getEmail() == null
                    || !StringValidationUtils.containsContent(member.getEmail().address())) {
                log.warn("[Membership Approved Event] Member person not found or without email | User: {}",
                        event.getMemberUserId());
                return;
            }

            String companyName = companyRepository.findById(event.getCompanyId())
                    .map(c -> c.getLegalName())
                    .orElse("");
            String role = companyRepository.findMembershipById(event.getMembershipId())
                    .map(m -> m.getRole().name())
                    .orElse("");

            Map<String, Object> templateData = new HashMap<>();
            templateData.put("name", member.getName());
            templateData.put("companyName", companyName);
            templateData.put("role", role);
            templateData.put("dashboardLink", frontendUrl + "/companies/" + event.getCompanyId());

            boolean isPortuguese = "pt".equalsIgnoreCase(member.getLocale());
            String subject = isPortuguese
                    ? "Solicitação aprovada - " + companyName
                    : "Request approved - " + companyName;
            Locale locale = isPortuguese
                    ? Locale.forLanguageTag("pt-BR")
                    : Locale.forLanguageTag("en-US");

            emailService.sendEmail(new EmailData(
                    member.getEmail().address(), subject, "membership_approved", templateData, locale));

            log.info("[Membership Approved Event] Approval email sent | User: {} | Company: {}",
                    event.getMemberUserId(), event.getCompanyId());
        } catch (Exception e) {
            log.error("[Membership Approved Event] Failed to send approval email | User: {}",
                    event.getMemberUserId(), e);
        }
    }
}
