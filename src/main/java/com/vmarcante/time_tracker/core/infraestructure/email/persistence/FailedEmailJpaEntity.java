package com.vmarcante.time_tracker.core.infraestructure.email.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.base.infraestructure.persistence.BaseEntityInterface;
import com.vmarcante.time_tracker.base.infraestructure.persistence.converter.EncryptedStringConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
@Entity
@Table(name = "TB0003_FAILED_EMAIL", schema = "dbo", indexes = {
        @Index(name = "IX0003_SQ_ID", columnList = "C0003_SQ_ID", unique = true),
        @Index(name = "IX0003_CREATED_AT", columnList = "C0003_CREATED_AT"),
        @Index(name = "IX0003_RECIPIENT", columnList = "C0003_RECIPIENT"),
        @Index(name = "IX0003_RETRY_COUNT", columnList = "C0003_RETRY_COUNT")
})
@Data
public class FailedEmailJpaEntity implements BaseEntityInterface<UUID, Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "C0003_FAILED_EMAIL_ID", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "C0003_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "C0003_UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "C0003_SQ_ID", nullable = false, updatable = false, insertable = false)
    private Integer seqId;

    @Column(name = "C0003_RECIPIENT", nullable = false, length = 255)
    private String recipient;

    @Column(name = "C0003_SUBJECT", nullable = false, length = 500)
    private String subject;

    @Column(name = "C0003_TEMPLATE_NAME", nullable = false, length = 100)
    private String templateName;

    @Column(name = "C0003_TEMPLATE_DATA", nullable = false, columnDefinition = "TEXT")
    @Convert(converter = EncryptedStringConverter.class)
    private String templateData;

    @Column(name = "C0003_LOCALE", nullable = false, length = 10)
    private String locale;

    @Column(name = "C0003_ERROR_MESSAGE", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "C0003_STACK_TRACE", columnDefinition = "TEXT")
    @Convert(converter = EncryptedStringConverter.class)
    private String stackTrace;

    @Column(name = "C0003_RETRY_COUNT", nullable = false)
    private Integer retryCount;

    @PrePersist
    public void onCreate() {
        setCreatedAt(LocalDateTime.now());
        setUpdatedAt(LocalDateTime.now());
        if (retryCount == null) {
            retryCount = 0;
        }
    }

    @PreUpdate
    public void onUpdate() {
        setUpdatedAt(LocalDateTime.now());
    }
}
