package com.vmarcante.time_tracker.core.domain.translation.service;

import java.util.Locale;

public interface TranslationService {

    boolean hasTranslation(String key, Locale locale);

    String translate(String key);

    String translate(String key, Locale locale);

    String translate(String key, Object... arguments);

    String translate(String key, Locale locale, Object... arguments);

}
