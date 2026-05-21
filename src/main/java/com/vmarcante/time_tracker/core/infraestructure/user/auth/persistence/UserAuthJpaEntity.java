package com.vmarcante.time_tracker.core.infraestructure.user.auth.persistence;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.base.infraestructure.persistence.BaseActiveEntityInterface;
import com.vmarcante.time_tracker.core.domain.user.enums.UserRoleType;
import com.vmarcante.time_tracker.core.infraestructure.person.persistence.PersonJpaEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
@Table(name = "TB0002_USER_AUTH", schema = "dbo", indexes = {
        @Index(name = "IX0002_USERNAME_LOWER", columnList = "C0002_USERNAME", unique = true),
        @Index(name = "IX0002_SQ_ID", columnList = "C0002_SQ_ID", unique = true),
        @Index(name = "IX0002_ACTIVE", columnList = "C0002_ACTIVE"),
        @Index(name = "IX0002_CREATED_AT", columnList = "C0002_CREATED_AT")
})
@Data
public class UserAuthJpaEntity implements BaseActiveEntityInterface<UUID, Integer> {

    // Basic Fields
    @Id
    @Column(name = "C0002_USER_ID", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "C0002_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "C0002_UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "C0002_SQ_ID", nullable = false, updatable = false, insertable = false)
    private Integer seqId;

    @Column(name = "C0002_ACTIVE", nullable = false)
    private Boolean active;

    // Entity Fields
    @Column(name = "C0002_USERNAME", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "C0002_PASSWORD_HASH", nullable = false, length = 512)
    private String password;

    @Column(name = "C0002_PASSWORD_SALT", nullable = false, length = 512)
    private String passwordSalt;

    @Column(name = "C0002_ACCESS_TOKEN", length = 512)
    private String accessToken;

    @Column(name = "C0002_USER_CONFIRMED", nullable = false)
    private Boolean userConfirmed;

    @Column(name = "C0002_FAILED_ATTEMPTS", nullable = false)
    private Integer failedAttempts;

    @Column(name = "C0002_LAST_PASSWORD_CHANGE")
    private LocalDateTime lastPasswordChange;

    @Column(name = "C0002_PASSWORD_RESET_REQUESTED", nullable = false)
    private Boolean passwordResetRequested;

    @Column(name = "C0002_LAST_PASSWORD_RESET_REQUEST")
    private LocalDateTime lastPasswordResetRequest;

    @Column(name = "C0002_LAST_LOGIN")
    private LocalDateTime lastLogin;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "C0002_ROLE", nullable = false)
    private UserRoleType role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "C0002_USER_ID", nullable = false, insertable = false, updatable = false)
    private PersonJpaEntity person;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.active == null) {
            this.active = true;
        }
        if (this.userConfirmed == null) {
            this.userConfirmed = false;
        }
        if (this.failedAttempts == null) {
            this.failedAttempts = 0;
        }
        if (this.passwordResetRequested == null) {
            this.passwordResetRequested = false;
        }

        if (this.role == null) {
            this.role = UserRoleType.USER;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
