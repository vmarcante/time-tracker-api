package com.vmarcante.time_tracker.core.infraestructure.user.session.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.base.infraestructure.persistence.BaseAuthEntityInterface;
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
@Table(name = "TB0004_USER_SESSION", schema = "dbo", indexes = {
        @Index(name = "IX0004_USER_ID", columnList = "C0004_USER_ID"),
        @Index(name = "IX0004_REFRESH_TOKEN", columnList = "C0004_REFRESH_TOKEN"),
        @Index(name = "IX0004_REFRESH_TOKEN_HASH", columnList = "C0004_REFRESH_TOKEN_HASH"),
        @Index(name = "IX0004_ACTIVE", columnList = "C0004_ACTIVE"),
        @Index(name = "IX0004_EXPIRY", columnList = "C0004_REFRESH_TOKEN_EXPIRY")
})
@Data
public class UserSessionJpaEntity implements BaseAuthEntityInterface<UUID, Integer> {

    // Basic Fields
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "C0004_SESSION_ID", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "C0004_SQ_ID", nullable = false, unique = true, updatable = false, insertable = false)
    private Integer seqId;

    @Column(name = "C0004_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "C0004_UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "C0004_CREATED_BY")
    private UUID createdBy;

    @Column(name = "C0004_UPDATED_BY")
    private UUID updatedBy;

    @Column(name = "C0004_ACTIVE", nullable = false)
    private Boolean active;

    // Entity Fields
    @Column(name = "C0004_USER_ID", nullable = false)
    private UUID userId;

    @Column(name = "C0004_REFRESH_TOKEN", nullable = false, columnDefinition = "TEXT")
    @Convert(converter = EncryptedStringConverter.class)
    private String refreshToken;

    @Column(name = "C0004_REFRESH_TOKEN_HASH", nullable = false, length = 64)
    private String refreshTokenHash;

    @Column(name = "C0004_REFRESH_TOKEN_EXPIRY", nullable = false)
    private LocalDateTime refreshTokenExpiry;

    @Column(name = "C0004_DEVICE_INFO", length = 255)
    private String deviceInfo;

    @Column(name = "C0004_IP_ADDRESS", length = 45)
    private String ipAddress;

    @Column(name = "C0004_USER_AGENT", length = 500)
    private String userAgent;

    @Column(name = "C0004_LAST_ACTIVITY_AT")
    private LocalDateTime lastActivityAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
