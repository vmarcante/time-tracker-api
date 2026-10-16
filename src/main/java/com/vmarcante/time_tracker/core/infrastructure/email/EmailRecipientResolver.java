package com.vmarcante.time_tracker.core.infrastructure.email;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.system.config.enums.SystemConfigKey;
import com.vmarcante.time_tracker.core.domain.system.config.model.SystemConfig;
import com.vmarcante.time_tracker.core.domain.system.config.repository.SystemConfigRepository;

/**
 * Resolves the actual recipient(s) for an outgoing email.
 * When {@code system_email_override} is configured with a non-blank value,
 * all outgoing emails are redirected to the addresses listed there (separated by ";").
 * Otherwise the original recipient is used.
 * When {@code system_email_override_include_original} is true, the original recipient
 * is also included in the resolved list.
 */
@Component
public class EmailRecipientResolver {

    private final SystemConfigRepository systemConfigRepository;

    public EmailRecipientResolver(SystemConfigRepository systemConfigRepository) {
        this.systemConfigRepository = systemConfigRepository;
    }

    public String[] resolve(String originalTo) {
        Map<SystemConfigKey, SystemConfig> configs = systemConfigRepository.findByKeys(List.of(
                SystemConfigKey.SYSTEM_EMAIL_OVERRIDE,
                SystemConfigKey.SYSTEM_EMAIL_OVERRIDE_INCLUDE_ORIGINAL));

        List<String> overrideEmails = Optional.ofNullable(configs.get(SystemConfigKey.SYSTEM_EMAIL_OVERRIDE))
                .map(config -> config.getValueAsList())
                .orElse(List.of());

        if (overrideEmails.isEmpty()) {
            return new String[]{originalTo};
        }

        boolean includeOriginal = Optional
                .ofNullable(configs.get(SystemConfigKey.SYSTEM_EMAIL_OVERRIDE_INCLUDE_ORIGINAL))
                .map(config -> config.getValueAsBoolean())
                .orElse(false);

        if (includeOriginal) {
            List<String> all = new ArrayList<>(overrideEmails);
            all.add(originalTo);
            return all.toArray(new String[0]);
        }

        return overrideEmails.toArray(new String[0]);
    }
}
