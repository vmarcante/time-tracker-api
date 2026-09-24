package com.vmarcante.time_tracker.core.infraestructure.company.event.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.event.MembershipRequestCreatedEvent;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.email.model.EmailData;
import com.vmarcante.time_tracker.core.domain.email.service.EmailService;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.shared.utils.StringValidationUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MembershipRequestCreatedEventListener {

    private final EmailService emailService;
    private final PersonRepository personRepository;
    private final CompanyRepository companyRepository;
    private final String frontendUrl;

    public MembershipRequestCreatedEventListener(
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
    public void handleMembershipRequestCreated(MembershipRequestCreatedEvent event) {
        try {
            Person requester = personRepository.findById(event.getRequesterUserId()).orElse(null);
            if (requester == null) {
                log.warn("[Membership Request Event] Requester person not found | User: {}",
                        event.getRequesterUserId());
                return;
            }

            List<CompanyMembership> approvers = companyRepository
                    .findApprovedMembershipsByCompanyId(event.getCompanyId(), Pageable.unpaged())
                    .getContent()
                    .stream()
                    .filter(m -> m.getRole() == CompanyRole.OWNER || m.getRole() == CompanyRole.ADMIN)
                    .toList();

            if (approvers.isEmpty()) {
                log.warn("[Membership Request Event] No approvers found for company {}", event.getCompanyId());
                return;
            }

            Set<UUID> approverIds = approvers.stream()
                    .filter(Objects::nonNull)
                    .map(CompanyMembership::getUserId)
                    .collect(Collectors.toSet());

            List<Person> approverPersons = personRepository.findAllByIds(approverIds);

            String companyName = companyRepository.findById(event.getCompanyId())
                    .map(c -> c.getLegalName())
                    .orElse("");
            
            String requestReason = companyRepository.findMembershipById(event.getMembershipId())
                    .map(CompanyMembership::getRequestReason)
                    .orElse(null);
                    
            String pendingLink = frontendUrl + "/companies/" + event.getCompanyId() + "/members/pending";

            for (Person approver : approverPersons) {
                sendToApprover(approver, requester, companyName, pendingLink, requestReason);
            }

            log.info("[Membership Request Event] Request emails sent to {} approver(s) | Company: {}",
                    approverPersons.size(), event.getCompanyId());
        } catch (Exception e) {
            log.error("[Membership Request Event] Failed to send request emails | Company: {}",
                    event.getCompanyId(), e);
        }
    }

    private void sendToApprover(Person approver, Person requester, String companyName, String pendingLink,
            String requestReason) {
        try {
            if (approver.getEmail() == null
                    || !StringValidationUtils.containsContent(approver.getEmail().address())) {
                return;
            }

            Map<String, Object> templateData = new HashMap<>();
            templateData.put("name", approver.getName());
            templateData.put("requesterName", requester.getName());
            templateData.put("requesterEmail",
                    requester.getEmail() != null ? requester.getEmail().address() : "");
            templateData.put("companyName", companyName);
            templateData.put("requestReason", requestReason);
            templateData.put("pendingLink", pendingLink);

            boolean isPortuguese = "pt".equalsIgnoreCase(approver.getLocale());
            String subject = isPortuguese
                    ? "Nova solicitação de vínculo - " + companyName
                    : "New membership request - " + companyName;
            Locale locale = isPortuguese
                    ? Locale.forLanguageTag("pt-BR")
                    : Locale.forLanguageTag("en-US");

            emailService.sendEmail(new EmailData(
                    approver.getEmail().address(), subject, "membership_request", templateData, locale));
        } catch (Exception e) {
            log.error("[Membership Request Event] Failed to send to approver {}", approver.getId(), e);
        }
    }
}
