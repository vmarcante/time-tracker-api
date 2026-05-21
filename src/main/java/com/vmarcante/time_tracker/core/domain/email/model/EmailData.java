package com.vmarcante.time_tracker.core.domain.email.model;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public record EmailData(
                String to,
                String subject,
                String templateName,
                Map<String, Object> templateData,
                Locale locale,
                List<EmailFile> attachments) {

        public EmailData(String to, String subject, String templateName, Map<String, Object> templateData) {
                this(to, subject, templateName, templateData, Locale.forLanguageTag("pt-BR"), Collections.emptyList());
        }

        public EmailData(String to, String subject, String templateName, Map<String, Object> templateData,
                        Locale locale) {
                this(to, subject, templateName, templateData, locale, Collections.emptyList());
        }

        public boolean hasAttachments() {
                return attachments != null && !attachments.isEmpty();
        }
}
