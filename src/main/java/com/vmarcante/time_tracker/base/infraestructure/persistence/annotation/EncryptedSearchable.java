package com.vmarcante.time_tracker.base.infraestructure.persistence.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as a search hash for an encrypted field.
 *
 * Place this annotation on the hash field (not the encrypted field).
 * The value of {@code sourceField} must be the name of the encrypted field
 * in the same entity whose value will be hashed into this field automatically
 * before persist and update via {@code EncryptedSearchableHashListener}.
 *
 * Example:
 * <pre>
 *   {@literal @}Column(name = "C0001_EMAIL")
 *   {@literal @}Convert(converter = EncryptedStringConverter.class)
 *   private String email;
 *
 *   {@literal @}Column(name = "C0001_EMAIL_HASH", length = 64)
 *   {@literal @}EncryptedSearchable(sourceField = "email")
 *   private String emailHash;
 * </pre>
 *
 * In the repository, query by the hash field:
 * <pre>
 *   Optional{@literal <}MyEntity{@literal >} findByEmailHash(String emailHash);
 * </pre>
 *
 * In the adapter, hash the search term before querying:
 * <pre>
 *   String hash = EncryptedFieldHashUtils.generateSearchHash(email);
 *   repository.findByEmailHash(hash);
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EncryptedSearchable {

    /**
     * Name of the encrypted source field in the same entity class.
     */
    String sourceField();

}
