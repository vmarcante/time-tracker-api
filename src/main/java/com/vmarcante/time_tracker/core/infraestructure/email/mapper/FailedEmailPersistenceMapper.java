package com.vmarcante.time_tracker.core.infraestructure.email.mapper;

import com.vmarcante.time_tracker.core.domain.email.model.EmailData;
import com.vmarcante.time_tracker.core.domain.email.model.FailedEmail;
import com.vmarcante.time_tracker.core.infraestructure.email.persistence.FailedEmailJpaEntity;

public class FailedEmailPersistenceMapper {

    public static FailedEmailJpaEntity toEntity(FailedEmail domain) {
        if (domain == null) {
            return null;
        }

        FailedEmailJpaEntity entity = new FailedEmailJpaEntity();
        entity.setId(domain.getId());
        entity.setSeqId(domain.getSeqId());
        entity.setRecipient(domain.getRecipient());
        entity.setSubject(domain.getSubject());
        entity.setTemplateName(domain.getTemplateName());
        entity.setTemplateData(domain.getTemplateData());
        entity.setLocale(domain.getLocale());
        entity.setErrorMessage(domain.getErrorMessage());
        entity.setStackTrace(domain.getStackTrace());
        entity.setRetryCount(domain.getRetryCount());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    public static FailedEmailJpaEntity emailDataToEntity(EmailData emailData, String templateDataJson,
            String errorMessage,
            String stackTrace) {
        if (emailData == null) {
            return null;
        }

        FailedEmail domain = fromEmailData(emailData, templateDataJson, errorMessage, stackTrace);
        return toEntity(domain);
    }

    public static FailedEmail toDomain(FailedEmailJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        FailedEmail domain = new FailedEmail();
        domain.setId(entity.getId());
        domain.setSeqId(entity.getSeqId());
        domain.setRecipient(entity.getRecipient());
        domain.setSubject(entity.getSubject());
        domain.setTemplateName(entity.getTemplateName());
        domain.setTemplateData(entity.getTemplateData());
        domain.setLocale(entity.getLocale());
        domain.setErrorMessage(entity.getErrorMessage());
        domain.setStackTrace(entity.getStackTrace());
        domain.setRetryCount(entity.getRetryCount());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }

    public static FailedEmail fromEmailData(EmailData emailData, String templateDataJson, String errorMessage,
            String stackTrace) {
        if (emailData == null) {
            return null;
        }

        FailedEmail domain = new FailedEmail();
        domain.setRecipient(emailData.to());
        domain.setSubject(emailData.subject());
        domain.setTemplateName(emailData.templateName());
        domain.setTemplateData(templateDataJson);
        domain.setLocale(emailData.locale() != null ? emailData.locale().toLanguageTag() : "pt-BR");
        domain.setErrorMessage(errorMessage);
        domain.setStackTrace(stackTrace);
        domain.setRetryCount(0);
        return domain;
    }
}
