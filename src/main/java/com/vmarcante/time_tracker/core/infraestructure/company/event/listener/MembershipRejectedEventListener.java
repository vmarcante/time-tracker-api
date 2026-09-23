package com.vmarcante.time_tracker.core.infraestructure.company.event.listener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.company.event.MembershipRejectedEvent;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.email.model.EmailData;
import com.vmarcante.time_tracker.core.domain.email.service.EmailService;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.shared.utils.StringValidationUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MembershipRejectedEventListener {

    private final EmailService emailService;
    private final PersonRepository personRepository;
    private final CompanyRepository companyRepository;
    private final String frontendUrl;

    public MembershipRejectedEventListener(
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
    public void handleMembershipRejected(MembershipRejectedEvent event) {
        try {
            Person member = personRepository.findById(event.getMemberUserId()).orElse(null);
            if (member == null || member.getEmail() == null
                    || !StringValidationUtils.containsContent(member.getEmail().address())) {
                log.warn("[Membership Rejected Event] Member person not found or without email | User: {}",
                        event.getMemberUserId());
                return;
            }

            String companyName = companyRepository.findById(event.getCompanyId())
                    .map(c -> c.getLegalName())
                    .orElse("");

            Map<String, Object> templateData = new HashMap<>();
            templateData.put("name", member.getName());
            templateData.put("companyName", companyName);
            templateData.put("homeLink", frontendUrl);

            boolean isPortuguese = "pt".equalsIgnoreCase(member.getLocale());
            String subject = isPortuguese
                    ? "Atualização sobre sua solicitação - " + companyName
                    : "Update on your request - " + companyName;
            Locale locale = isPortuguese
                    ? Locale.forLanguageTag("pt-BR")
                    : Locale.forLanguageTag("en-US");

            emailService.sendEmail(new EmailData(
                    member.getEmail().address(), subject, "membership_rejected", templateData, locale));

            log.info("[Membership Rejected Event] Rejection email sent | User: {} | Company: {}",
                    event.getMemberUserId(), event.getCompanyId());
        } catch (Exception e) {
            log.error("[Membership Rejected Event] Failed to send rejection email | User: {}",
                    event.getMemberUserId(), e);
        }
    }
}
