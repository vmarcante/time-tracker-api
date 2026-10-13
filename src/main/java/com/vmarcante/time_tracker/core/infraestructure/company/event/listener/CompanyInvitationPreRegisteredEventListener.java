package com.vmarcante.time_tracker.core.infraestructure.company.event.listener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.company.event.CompanyInvitationPreRegisteredEvent;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.email.model.EmailData;
import com.vmarcante.time_tracker.core.domain.email.service.EmailService;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CompanyInvitationPreRegisteredEventListener {

    private final EmailService emailService;
    private final PersonRepository personRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final CompanyRepository companyRepository;
    private final String frontendUrl;

    public CompanyInvitationPreRegisteredEventListener(
            EmailService emailService,
            PersonRepository personRepository,
            CompanyMembershipRepository membershipRepository,
            CompanyRepository companyRepository,
            @Value("${app.frontend.url:https://app.timetracker.com}") String frontendUrl) {
        this.emailService = emailService;
        this.personRepository = personRepository;
        this.membershipRepository = membershipRepository;
        this.companyRepository = companyRepository;
        this.frontendUrl = frontendUrl;
    }

    @Async
    @EventListener
    public void handleCompanyInvitationPreRegistered(CompanyInvitationPreRegisteredEvent event) {
        try {
            String companyName = companyRepository.findById(event.getCompanyId())
                    .map(c -> c.getLegalName())
                    .orElse("");
            String inviterName = personRepository.findNameById(event.getInviterUserId()).orElse("");
            String role = membershipRepository.findMembershipById(event.getMembershipId())
                    .map(m -> m.getRole().name())
                    .orElse("");

            Map<String, Object> templateData = new HashMap<>();
            templateData.put("companyName", companyName);
            templateData.put("inviterName", inviterName);
            templateData.put("role", role);
            templateData.put("registerLink", frontendUrl + "/register");

            String subject = "Você foi pré-cadastrado em " + companyName + " - Time Tracker";
            Locale locale = Locale.forLanguageTag("pt-BR");

            emailService.sendEmail(new EmailData(
                    event.getInviteeEmail(), subject, "company_invite_pre_registered", templateData, locale));

            log.info("[Pre-Registered Invite Event] Email sent to {} | Company: {}",
                    event.getInviteeEmail(), event.getCompanyId());
        } catch (Exception e) {
            log.error("[Pre-Registered Invite Event] Failed to send email to {}",
                    event.getInviteeEmail(), e);
        }
    }
}
