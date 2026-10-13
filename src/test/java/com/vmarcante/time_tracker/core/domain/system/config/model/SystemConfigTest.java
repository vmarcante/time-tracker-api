package com.vmarcante.time_tracker.core.domain.system.config.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.vmarcante.time_tracker.core.domain.system.config.enums.SystemConfigValueType;

class SystemConfigTest {

    @Test
    void shouldReturnEmptyListWhenValueIsNull() {
        // Arrange
        SystemConfig config = new SystemConfig();
        config.setValue(null);

        // Act
        List<String> result = config.getValueAsList();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenValueIsBlank() {
        // Arrange
        SystemConfig config = new SystemConfig();
        config.setValue("   ");

        // Act
        List<String> result = config.getValueAsList();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnSingleEmailWhenNoSemicolon() {
        // Arrange
        SystemConfig config = new SystemConfig();
        config.setValueType(SystemConfigValueType.STRING_LIST);
        config.setValue("user@example.com");

        // Act
        List<String> result = config.getValueAsList();

        // Assert
        assertThat(result).containsExactly("user@example.com");
    }

    @Test
    void shouldReturnMultipleEmailsWhenSeparatedBySemicolon() {
        // Arrange
        SystemConfig config = new SystemConfig();
        config.setValueType(SystemConfigValueType.STRING_LIST);
        config.setValue("user1@example.com;user2@example.com;user3@example.com");

        // Act
        List<String> result = config.getValueAsList();

        // Assert
        assertThat(result).containsExactly("user1@example.com", "user2@example.com", "user3@example.com");
    }

    @Test
    void shouldTrimWhitespaceAroundEmails() {
        // Arrange
        SystemConfig config = new SystemConfig();
        config.setValueType(SystemConfigValueType.STRING_LIST);
        config.setValue("  user1@example.com ; user2@example.com  ");

        // Act
        List<String> result = config.getValueAsList();

        // Assert
        assertThat(result).containsExactly("user1@example.com", "user2@example.com");
    }

    @Test
    void shouldFilterEmptySegmentsFromConsecutiveSemicolons() {
        // Arrange
        SystemConfig config = new SystemConfig();
        config.setValueType(SystemConfigValueType.STRING_LIST);
        config.setValue("user1@example.com;;user2@example.com;");

        // Act
        List<String> result = config.getValueAsList();

        // Assert
        assertThat(result).containsExactly("user1@example.com", "user2@example.com");
    }
}
