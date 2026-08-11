package com.vmarcante.time_tracker.base.infraestructure.persistence.listener;

import java.lang.reflect.Field;

import com.vmarcante.time_tracker.base.infraestructure.persistence.annotation.EncryptedSearchable;
import com.vmarcante.time_tracker.base.infraestructure.persistence.converter.EncryptedFieldHashUtils;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;

/**
 * JPA entity listener that automatically populates hash fields annotated with
 * {@code @EncryptedSearchable} before persist and update operations.
 *
 * Register on the entity with {@code @EntityListeners(EncryptedSearchableHashListener.class)}.
 */
@Slf4j
public class EncryptedSearchableHashListener {

    @PrePersist
    @PreUpdate
    public void populateSearchHashes(Object entity) {
        Class<?> entityClass = entity.getClass();
        Field[] fields = entityClass.getDeclaredFields();

        for (Field hashField : fields) {
            EncryptedSearchable annotation = hashField.getAnnotation(EncryptedSearchable.class);
            if (annotation == null) {
                continue;
            }

            String sourceFieldName = annotation.sourceField();

            try {
                Field sourceField = entityClass.getDeclaredField(sourceFieldName);
                sourceField.setAccessible(true);
                hashField.setAccessible(true);

                Object sourceValue = sourceField.get(entity);
                if (sourceValue instanceof String stringValue) {
                    String hash = EncryptedFieldHashUtils.generateSearchHash(stringValue);
                    hashField.set(entity, hash);
                }

            } catch (NoSuchFieldException e) {
                log.error("[EncryptedSearchableHash] Source field '{}' not found on entity '{}'",
                        sourceFieldName, entityClass.getSimpleName());
            } catch (IllegalAccessException e) {
                log.error("[EncryptedSearchableHash] Cannot access field '{}' on entity '{}'",
                        sourceFieldName, entityClass.getSimpleName());
            }
        }
    }

}
