package com.vmarcante.time_tracker.core.infraestructure.email;

import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailTemplateService {

    private final TemplateEngine templateEngine;

    public EmailTemplateService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String processTemplate(String templateName, Map<String, Object> templateData, Locale locale) {
        log.debug("[ Email Template ] - Processing template: {} | Locale: {}", templateName, locale);

        Context context = new Context(locale);
        if (templateData != null) {
            templateData.forEach(context::setVariable);
        }

        String templatePath = resolveTemplatePath(templateName, locale);
        
        try {
            String htmlContent = templateEngine.process(templatePath, context);
            log.debug("[ Email Template ] - Template processed successfully: {}", templatePath);
            return htmlContent;
        } catch (Exception e) {
            log.warn("[ Email Template ] - Template not found: {}, falling back to Portuguese", templatePath);
            String fallbackPath = "email/" + templateName + "_pt";
            String htmlContent = templateEngine.process(fallbackPath, context);
            log.debug("[ Email Template ] - Fallback template processed successfully: {}", fallbackPath);
            return htmlContent;
        }
    }

    private String resolveTemplatePath(String templateName, Locale locale) {
        String languageCode = locale.getLanguage();
        String localizedTemplate = "email/" + templateName + "_" + languageCode;
        
        log.debug("[ Email Template ] - Resolved template path: {}", localizedTemplate);
        return localizedTemplate;
    }
}
