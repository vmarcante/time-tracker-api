package com.vmarcante.time_tracker.core.infraestructure.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.domain.email.model.EmailData;
import com.vmarcante.time_tracker.core.domain.email.model.EmailFile;
import com.vmarcante.time_tracker.core.domain.email.service.EmailService;
import com.vmarcante.time_tracker.core.infraestructure.email.persistence.FailedEmailPersistenceAdapter;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailSenderService implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateService templateService;
    private final FailedEmailPersistenceAdapter failedEmailAdapter;
    private final String fromEmail;

    public EmailSenderService(
            JavaMailSender mailSender,
            EmailTemplateService templateService,
            FailedEmailPersistenceAdapter failedEmailAdapter,
            @Value("${email.from.address:noreply@timetracker.com}") String fromEmail,
            @Value("${email.from.name:Time Tracker}") String fromName) {
        this.mailSender = mailSender;
        this.templateService = templateService;
        this.failedEmailAdapter = failedEmailAdapter;
        this.fromEmail = fromName + " <" + fromEmail + ">";
    }

    @Override
    public void sendEmail(EmailData emailData) throws Exception {
        sendEmail(emailData, true);
    }

    public void sendEmail(EmailData emailData, boolean trackFailure) throws Exception {
        log.debug(
                "[ Email Sender ] - Starting email dispatch | Recipient: {} | Subject: {} | Template: {} | Locale: {} | Attachments: {}",
                emailData.to(), emailData.subject(), emailData.templateName(), emailData.locale(),
                emailData.hasAttachments() ? emailData.attachments().size() : 0);

        String htmlContent = templateService.processTemplate(
                emailData.templateName(),
                emailData.templateData(),
                emailData.locale());

        // TODO - REMOVE OVERRIDE
        String emailTo = emailData.to();
        emailTo = "vinirosamarcante@gmail.com";

        try {
            if (emailData.hasAttachments()) {
                sendEmailWithAttachments(emailTo, emailData.subject(), htmlContent, emailData);
            } else {
                sendEmail(emailTo, emailData.subject(), htmlContent);
            }
            log.debug("[ Email Sender ] - Email dispatched successfully to: {}", emailTo);
        } catch (Exception e) {

            if (trackFailure) {
                log.error("[ Email Sender ] - Failed to send email, saving to database for retry | Recipient: {}",
                        emailData.to(), e);
                failedEmailAdapter.save(emailData, e);
            } else {
                log.error("[ Email Sender ] - Failed to send email | Recipient: {}", emailData.to(), e);
            }
            throw e;
        }
    }

    @Override
    public void sendEmail(String to, String subject, String htmlContent) throws Exception {
        log.info("[ Email Sender ] - Sending email to: {} | Subject: {}", to, subject);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("[ Email Sender ] - Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("[ Email Sender ] - Failed to send email to: {}", to, e);
            throw new Exception("Failed to send email: " + e.getMessage(), e);
        }
    }

    private void sendEmailWithAttachments(String to, String subject, String htmlContent, EmailData emailData)
            throws Exception {
        log.info("[ Email Sender ] - Sending email with {} attachment(s) to: {} | Subject: {}",
                emailData.attachments().size(), to, subject);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            for (EmailFile attachment : emailData.attachments()) {
                if (attachment.isInline()) {
                    log.debug("[ Email Sender ] - Adding inline attachment: {} | Type: {} | Size: {} bytes | CID: {}",
                            attachment.name(), attachment.mimeType(), attachment.size(), attachment.contentId());

                    helper.addInline(
                            attachment.contentId(),
                            new ByteArrayResource(attachment.binary()),
                            attachment.mimeType());

                    continue;
                }

                log.debug("[ Email Sender ] - Adding attachment: {} | Type: {} | Size: {} bytes",
                        attachment.name(), attachment.mimeType(), attachment.size());

                helper.addAttachment(
                        attachment.name(),
                        new ByteArrayResource(attachment.binary()),
                        attachment.mimeType());

            }

            mailSender.send(message);
            log.info("[ Email Sender ] - Email with attachments sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("[ Email Sender ] - Failed to send email with attachments to: {}", to, e);
            throw new Exception("Failed to send email with attachments: " + e.getMessage(), e);
        }
    }
}
