package com.vmarcante.time_tracker.core.application.email.use_cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.vmarcante.time_tracker.core.domain.email.model.EmailData;
import com.vmarcante.time_tracker.core.domain.email.model.FailedEmail;
import com.vmarcante.time_tracker.core.domain.email.repository.FailedEmailRepository;
import com.vmarcante.time_tracker.core.domain.email.service.EmailService;

class RetryFailedEmailUseCaseImplTest {

    private static final int MAX_RETRY_ATTEMPTS = 5;

    private final FailedEmailRepository failedEmailRepository = mock(FailedEmailRepository.class);
    private final EmailService emailService = mock(EmailService.class);
    private final RetryFailedEmailUseCaseImpl useCase = new RetryFailedEmailUseCaseImpl(
            failedEmailRepository, emailService, MAX_RETRY_ATTEMPTS);

    @Test
    void givenRetryableEmail_whenSendSucceeds_thenShouldDeleteFailedEmail() throws Exception {
        // Arrange
        FailedEmail failedEmail = buildFailedEmail();
        when(failedEmailRepository.findAllRetryable(MAX_RETRY_ATTEMPTS)).thenReturn(List.of(failedEmail));

        // Act
        useCase.execute();

        // Assert
        verify(emailService).sendEmail(any(EmailData.class), eq(false));
        verify(failedEmailRepository).deleteById(failedEmail.getId());
        verify(failedEmailRepository, never()).update(any(FailedEmail.class));
    }

    @Test
    void givenRetryableEmail_whenSendFails_thenShouldIncrementRetryCount() throws Exception {
        // Arrange
        FailedEmail failedEmail = buildFailedEmail();
        when(failedEmailRepository.findAllRetryable(MAX_RETRY_ATTEMPTS)).thenReturn(List.of(failedEmail));
        doThrow(new RuntimeException("SMTP error")).when(emailService).sendEmail(any(EmailData.class), eq(false));

        // Act
        useCase.execute();

        // Assert
        verify(failedEmailRepository).update(failedEmail);
        assertThat(failedEmail.getRetryCount()).isEqualTo(2);
        verify(failedEmailRepository, never()).deleteById(any());
    }

    @Test
    void givenNoRetryableEmails_whenExecuting_thenShouldDoNothing() throws Exception {
        // Arrange
        when(failedEmailRepository.findAllRetryable(MAX_RETRY_ATTEMPTS)).thenReturn(List.of());

        // Act
        useCase.execute();

        // Assert
        verify(emailService, never()).sendEmail(any(EmailData.class), eq(false));
        verify(failedEmailRepository, never()).deleteById(any());
        verify(failedEmailRepository, never()).update(any(FailedEmail.class));
    }

    private FailedEmail buildFailedEmail() {
        FailedEmail failedEmail = new FailedEmail();
        failedEmail.setId(java.util.UUID.randomUUID());
        failedEmail.setRecipient("user@example.com");
        failedEmail.setSubject("Subject");
        failedEmail.setTemplateName("template");
        failedEmail.setTemplateData("{}");
        failedEmail.setLocale("pt-BR");
        failedEmail.setRetryCount(1);
        return failedEmail;
    }
}
