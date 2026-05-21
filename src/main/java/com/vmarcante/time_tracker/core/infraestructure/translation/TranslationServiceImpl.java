package com.vmarcante.time_tracker.core.infraestructure.translation;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.domain.translation.service.TranslationService;
import com.vmarcante.time_tracker.core.shared.utils.StringValidationUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TranslationServiceImpl implements TranslationService {

    private final MessageSource messageSource;

    public TranslationServiceImpl(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String translate(String key, Locale locale) {
        return translate(key, locale, (Object[]) null);
    }

    @Override
    public String translate(String key, Locale locale, Object... arguments) {
        try {
            return messageSource.getMessage(key, arguments, locale);
        } catch (NoSuchMessageException e) {
            log.warn("[Translation Service] Translation not found: key={}, locale={}", key, locale);
            return key; // Fallback to key if translation not found
        }
    }

    @Override
    public String translate(String key) {
        return translate(key, LocaleContextHolder.getLocale());
    }

    @Override
    public String translate(String key, Object... arguments) {
        return translate(key, LocaleContextHolder.getLocale(), arguments);
    }

    @Override
    public boolean hasTranslation(String key, Locale locale) {
        try {
            String message = messageSource.getMessage(key, null, locale);
            return StringValidationUtils.containsContent(message);
        } catch (NoSuchMessageException e) {
            return false;
        }
    }
}
