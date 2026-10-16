package com.vmarcante.time_tracker.core.infrastructure.company.event.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.vmarcante.time_tracker.core.application.company.membership.policy.MembershipApproverPolicy;
import com.vmarcante.time_tracker.core.domain.company.event.MembershipRequestCreatedEvent;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
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
    private final CompanyMembershipRepository membershipRepository;
    private final CompanyRepository companyRepository;
    private final MembershipApproverPolicy approverPolicy;
    private final String frontendUrl;

    public MembershipRequestCreatedEventListener(
            EmailService emailService,
            PersonRepository personRepository,
            CompanyMembershipRepository membershipRepository,
            CompanyRepository companyRepository,
            MembershipApproverPolicy approverPolicy,
            @Value("${app.frontend.url:https://app.timetracker.com}") String frontendUrl) {
        this.emailService = emailService;
        this.personRepository = personRepository;
        this.membershipRepository = membershipRepository;
        this.companyRepository = companyRepository;
        this.approverPolicy = approverPolicy;
        this.frontendUrl = frontendUrl;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleMembershipRequestCreated(MembershipRequestCreatedEvent event) {
        try {
            Person requester = personRepository.findById(event.getRequesterUserId()).orElse(null);
            if (requester == null) {
                log.warn("[Membership Request Event] Requester person not found | User: {}",
                        event.getRequesterUserId());
                return;
            }

            CompanyMembershipFilter companyMembershipFilter = CompanyMembershipFilterMapper.approvedByCompany(event.getCompanyId());
            List<CompanyMembership> approvers = approverPolicy.selectApprovers(
                    membershipRepository.search(companyMembershipFilter, Pageable.unpaged())
                            .getContent());

            if (approvers.isEmpty()) {
                log.warn("[Membership Request Event] No approvers found for company {}", event.getCompanyId());
                return;
            }

            Set<UUID> approverIds = approvers.stream()
                    .filter(Objects::nonNull)
                    .map(m -> m.getUserId())
                    .collect(Collectors.toSet());

            List<Person> approverPersons = personRepository.findAllByIds(approverIds);

            String companyName = companyRepository.findById(event.getCompanyId())
                    .map(c -> c.getLegalName())
                    .orElse("");
            
            String requestReason = membershipRepository.findById(event.getMembershipId())
                    .map(m -> m.getRequestReason())
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
