package com.vmarcante.time_tracker.core.infrastructure.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.vmarcante.time_tracker.core.domain.system.config.enums.SystemConfigKey;
import com.vmarcante.time_tracker.core.domain.system.config.enums.SystemConfigValueType;
import com.vmarcante.time_tracker.core.domain.system.config.model.SystemConfig;
import com.vmarcante.time_tracker.core.domain.system.config.repository.SystemConfigRepository;

class EmailRecipientResolverTest {

    private final SystemConfigRepository repository = mock(SystemConfigRepository.class);
    private final EmailRecipientResolver resolver = new EmailRecipientResolver(repository);

    @Test
    void shouldReturnOriginalRecipientWhenOverrideNotConfigured() {
        // Arrange
        when(repository.findByKeys(anyList())).thenReturn(Map.of());

        // Act
        String[] result = resolver.resolve("original@example.com");

        // Assert
        assertThat(result).containsExactly("original@example.com");
    }

    @Test
    void shouldReturnOriginalRecipientWhenOverrideValueIsNull() {
        // Arrange
        SystemConfig config = buildOverrideConfig(null);
        when(repository.findByKeys(anyList()))
                .thenReturn(Map.of(SystemConfigKey.SYSTEM_EMAIL_OVERRIDE, config));

        // Act
        String[] result = resolver.resolve("original@example.com");

        // Assert
        assertThat(result).containsExactly("original@example.com");
    }

    @Test
    void shouldReturnOriginalRecipientWhenOverrideValueIsBlank() {
        // Arrange
        SystemConfig config = buildOverrideConfig("   ");
        when(repository.findByKeys(anyList()))
                .thenReturn(Map.of(SystemConfigKey.SYSTEM_EMAIL_OVERRIDE, config));

        // Act
        String[] result = resolver.resolve("original@example.com");

        // Assert
        assertThat(result).containsExactly("original@example.com");
    }

    @Test
    void shouldRedirectToSingleOverrideEmail() {
        // Arrange
        SystemConfig overrideConfig = buildOverrideConfig("override@example.com");
        when(repository.findByKeys(anyList()))
                .thenReturn(Map.of(SystemConfigKey.SYSTEM_EMAIL_OVERRIDE, overrideConfig));

        // Act
        String[] result = resolver.resolve("original@example.com");

        // Assert
        assertThat(result).containsExactly("override@example.com");
    }

    @Test
    void shouldRedirectToMultipleOverrideEmails() {
        // Arrange
        SystemConfig overrideConfig = buildOverrideConfig("dev1@example.com;dev2@example.com;dev3@example.com");
        when(repository.findByKeys(anyList()))
                .thenReturn(Map.of(SystemConfigKey.SYSTEM_EMAIL_OVERRIDE, overrideConfig));

        // Act
        String[] result = resolver.resolve("original@example.com");

        // Assert
        assertThat(result).containsExactly("dev1@example.com", "dev2@example.com", "dev3@example.com");
    }

    @Test
    void shouldTrimWhitespaceInOverrideEmails() {
        // Arrange
        SystemConfig overrideConfig = buildOverrideConfig("  dev1@example.com ; dev2@example.com  ");
        when(repository.findByKeys(anyList()))
                .thenReturn(Map.of(SystemConfigKey.SYSTEM_EMAIL_OVERRIDE, overrideConfig));

        // Act
        String[] result = resolver.resolve("original@example.com");

        // Assert
        assertThat(result).containsExactly("dev1@example.com", "dev2@example.com");
    }

    @Test
    void shouldIncludeOriginalWhenIncludeOriginalIsTrue() {
        // Arrange
        SystemConfig overrideConfig = buildOverrideConfig("dev1@example.com;dev2@example.com");
        SystemConfig includeConfig = buildIncludeOriginalConfig("true");
        when(repository.findByKeys(anyList()))
                .thenReturn(Map.of(
                        SystemConfigKey.SYSTEM_EMAIL_OVERRIDE, overrideConfig,
                        SystemConfigKey.SYSTEM_EMAIL_OVERRIDE_INCLUDE_ORIGINAL, includeConfig));

        // Act
        String[] result = resolver.resolve("original@example.com");

        // Assert
        assertThat(result).containsExactly("dev1@example.com", "dev2@example.com", "original@example.com");
    }

    @Test
    void shouldNotIncludeOriginalWhenIncludeOriginalIsFalse() {
        // Arrange
        SystemConfig overrideConfig = buildOverrideConfig("dev1@example.com");
        SystemConfig includeConfig = buildIncludeOriginalConfig("false");
        when(repository.findByKeys(anyList()))
                .thenReturn(Map.of(
                        SystemConfigKey.SYSTEM_EMAIL_OVERRIDE, overrideConfig,
                        SystemConfigKey.SYSTEM_EMAIL_OVERRIDE_INCLUDE_ORIGINAL, includeConfig));

        // Act
        String[] result = resolver.resolve("original@example.com");

        // Assert
        assertThat(result).containsExactly("dev1@example.com");
    }

    @Test
    void shouldNotIncludeOriginalWhenIncludeOriginalConfigNotFound() {
        // Arrange
        SystemConfig overrideConfig = buildOverrideConfig("dev1@example.com");
        when(repository.findByKeys(anyList()))
                .thenReturn(Map.of(SystemConfigKey.SYSTEM_EMAIL_OVERRIDE, overrideConfig));

        // Act
        String[] result = resolver.resolve("original@example.com");

        // Assert
        assertThat(result).containsExactly("dev1@example.com");
    }

    private SystemConfig buildOverrideConfig(String value) {
        SystemConfig config = new SystemConfig();
        config.setKey(SystemConfigKey.SYSTEM_EMAIL_OVERRIDE);
        config.setValueType(SystemConfigValueType.STRING_LIST);
        config.setValue(value);
        return config;
    }

    private SystemConfig buildIncludeOriginalConfig(String value) {
        SystemConfig config = new SystemConfig();
        config.setKey(SystemConfigKey.SYSTEM_EMAIL_OVERRIDE_INCLUDE_ORIGINAL);
        config.setValueType(SystemConfigValueType.BOOLEAN);
        config.setValue(value);
        return config;
    }
}
