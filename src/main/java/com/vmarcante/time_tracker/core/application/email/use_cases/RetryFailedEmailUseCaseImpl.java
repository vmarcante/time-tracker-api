package com.vmarcante.time_tracker.core.application.email.use_cases;

import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.email.in.RetryFailedEmailUseCase;
import com.vmarcante.time_tracker.core.domain.email.model.EmailData;
import com.vmarcante.time_tracker.core.domain.email.model.FailedEmail;
import com.vmarcante.time_tracker.core.domain.email.repository.FailedEmailRepository;
import com.vmarcante.time_tracker.core.domain.email.service.EmailService;
import com.vmarcante.time_tracker.core.shared.utils.JsonUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RetryFailedEmailUseCaseImpl implements RetryFailedEmailUseCase {

    private final FailedEmailRepository failedEmailRepository;
    private final EmailService emailService;

    public RetryFailedEmailUseCaseImpl(
            FailedEmailRepository failedEmailRepository,
            EmailService emailService) {
        this.failedEmailRepository = failedEmailRepository;
        this.emailService = emailService;
    }

    @Override
    public boolean execute(FailedEmail failedEmail) {
        log.debug("[Retry Failed Email] Attempting to retry email | ID: {} | Recipient: {} | Retry count: {}",
                failedEmail.getId(), failedEmail.getRecipient(), failedEmail.getRetryCount());

        try {
            Map<String, Object> templateData = JsonUtils.deserializeToMap(failedEmail.getTemplateData());
            Locale locale = failedEmail.getLocale() != null
                    ? Locale.forLanguageTag(failedEmail.getLocale())
                    : Locale.forLanguageTag("pt-BR");

            EmailData emailData = new EmailData(
                    failedEmail.getRecipient(),
                    failedEmail.getSubject(),
                    failedEmail.getTemplateName(),
                    templateData,
                    locale,
                    null);

            emailService.sendEmail(emailData, false);

            failedEmailRepository.deleteById(failedEmail.getId());

            log.info("[Retry Failed Email] Email sent successfully | ID: {} | Recipient: {}",
                    failedEmail.getId(), failedEmail.getRecipient());

            return true;

        } catch (Exception e) {
            log.warn("[Retry Failed Email] Failed to send email | ID: {} | Recipient: {} | Error: {}",
                    failedEmail.getId(), failedEmail.getRecipient(), e.getMessage());

            failedEmail.setRetryCount(failedEmail.getRetryCount() + 1);
            failedEmailRepository.update(failedEmail);

            return false;
        }
    }
}
