package com.vmarcante.time_tracker.core.application.person.policy;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

class PersonInputValidationPolicyTest {

    private final PersonInputValidationPolicy policy = new PersonInputValidationPolicy();

    @Test
    void givenValidFullName_whenValidatingName_thenShouldPass() {
        // Arrange
        String name = "Maria Silva";

        // Act & Assert
        assertThatCode(() -> policy.validateName(name)).doesNotThrowAnyException();
    }

    @Test
    void givenNullName_whenValidatingName_thenShouldThrowRequired() {
        // Act & Assert
        assertThatThrownBy(() -> policy.validateName(null))
                .isInstanceOf(ApplicationException.class)
                .extracting("exceptionKey")
                .isEqualTo("user.name.required");
    }

    @Test
    void givenBlankName_whenValidatingName_thenShouldThrowRequired() {
        // Act & Assert
        assertThatThrownBy(() -> policy.validateName("   "))
                .isInstanceOf(ApplicationException.class)
                .extracting("exceptionKey")
                .isEqualTo("user.name.required");
    }

    @Test
    void givenNameWithLeadingOrTrailingSpaces_whenValidatingName_thenShouldThrowInvalidSpacing() {
        // Act & Assert
        assertThatThrownBy(() -> policy.validateName(" Maria Silva"))
                .isInstanceOf(ApplicationException.class)
                .extracting("exceptionKey")
                .isEqualTo("user.name.invalid.spacing");

        assertThatThrownBy(() -> policy.validateName("Maria Silva "))
                .isInstanceOf(ApplicationException.class)
                .extracting("exceptionKey")
                .isEqualTo("user.name.invalid.spacing");
    }

    @Test
    void givenShortName_whenValidatingName_thenShouldThrowMinLength() {
        // Act & Assert
        assertThatThrownBy(() -> policy.validateName("Ab"))
                .isInstanceOf(ApplicationException.class)
                .extracting("exceptionKey")
                .isEqualTo("user.name.min.length");
    }

    @Test
    void givenNameWithDigits_whenValidatingName_thenShouldThrowInvalidCharacters() {
        // Act & Assert
        assertThatThrownBy(() -> policy.validateName("Jo4o Silva"))
                .isInstanceOf(ApplicationException.class)
                .extracting("exceptionKey")
                .isEqualTo("user.name.invalid.characters");
    }

    @Test
    void givenNameWithMultipleSpaces_whenValidatingName_thenShouldThrowMultipleSpaces() {
        // Act & Assert
        assertThatThrownBy(() -> policy.validateName("Maria  Silva"))
                .isInstanceOf(ApplicationException.class)
                .extracting("exceptionKey")
                .isEqualTo("user.name.multiple.spaces");
    }

    @Test
    void givenSingleName_whenValidatingName_thenShouldThrowMissingSurname() {
        // Act & Assert
        assertThatThrownBy(() -> policy.validateName("Maria"))
                .isInstanceOf(ApplicationException.class)
                .extracting("exceptionKey")
                .isEqualTo("user.name.missing.surname");
    }

    @Test
    void givenNameWithShortPart_whenValidatingName_thenShouldThrowPartTooShort() {
        // Act & Assert
        assertThatThrownBy(() -> policy.validateName("Maria S"))
                .isInstanceOf(ApplicationException.class)
                .extracting("exceptionKey")
                .isEqualTo("user.name.part.too.short");
    }

    @Test
    void givenNullAge_whenValidatingRequiredAge_thenShouldThrowRequired() {
        // Act & Assert
        assertThatThrownBy(() -> policy.validateRequiredAge(null))
                .isInstanceOf(ApplicationException.class)
                .extracting("exceptionKey")
                .isEqualTo("user.age.required");
    }

    @Test
    void givenNullAge_whenValidatingOptionalAge_thenShouldPass() {
        // Act & Assert
        assertThatCode(() -> policy.validateOptionalAge(null)).doesNotThrowAnyException();
    }

    @Test
    void givenAgeBelowMinimum_whenValidatingAge_thenShouldThrowMin() {
        // Act & Assert
        assertThatThrownBy(() -> policy.validateRequiredAge(17))
                .isInstanceOf(ApplicationException.class)
                .extracting("exceptionKey")
                .isEqualTo("user.age.min");

        assertThatThrownBy(() -> policy.validateOptionalAge(17))
                .isInstanceOf(ApplicationException.class)
                .extracting("exceptionKey")
                .isEqualTo("user.age.min");
    }

    @Test
    void givenAgeAboveMaximum_whenValidatingAge_thenShouldThrowMax() {
        // Act & Assert
        assertThatThrownBy(() -> policy.validateRequiredAge(121))
                .isInstanceOf(ApplicationException.class)
                .extracting("exceptionKey")
                .isEqualTo("user.age.max");

        assertThatThrownBy(() -> policy.validateOptionalAge(121))
                .isInstanceOf(ApplicationException.class)
                .extracting("exceptionKey")
                .isEqualTo("user.age.max");
    }

    @Test
    void givenValidAge_whenValidatingAge_thenShouldPass() {
        // Act & Assert
        assertThatCode(() -> policy.validateRequiredAge(30)).doesNotThrowAnyException();
        assertThatCode(() -> policy.validateOptionalAge(69)).doesNotThrowAnyException();
    }
}
