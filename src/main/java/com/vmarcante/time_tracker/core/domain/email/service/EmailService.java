package com.vmarcante.time_tracker.core.domain.email.service;

import com.vmarcante.time_tracker.core.domain.email.model.EmailData;

public interface EmailService {

    void sendEmail(String to, String subject, String htmlContent) throws Exception;

    void sendEmail(EmailData emailData) throws Exception;

    void sendEmail(EmailData emailData, boolean trackFailure) throws Exception;
}
