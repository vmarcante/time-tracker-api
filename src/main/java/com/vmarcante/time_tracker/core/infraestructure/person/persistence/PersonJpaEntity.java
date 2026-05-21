package com.vmarcante.time_tracker.core.infraestructure.person.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.base.infraestructure.persistence.BaseAuthEntityInterface;

import jakarta.persistence.Column;
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
@Table(name = "TB0001_PERSON", schema = "dbo", indexes = {
        @Index(name = "IX0001_ID", columnList = "C0001_ID"),
        @Index(name = "IX0001_SQ_ID", columnList = "C0001_SQ_ID", unique = true),
        @Index(name = "IX0001_EMAIL_LOWER", columnList = "C0001_EMAIL", unique = true),
        @Index(name = "IX0001_CREATED_AT", columnList = "C0001_CREATED_AT")
})
@Data
public class PersonJpaEntity implements BaseAuthEntityInterface<UUID, Integer> {

    // Basic Fields
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "C0001_ID")
    private UUID id;

    @Column(name = "C0001_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "C0001_UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "C0001_ACTIVE", nullable = false)
    private Boolean active;

    @Column(name = "C0001_SQ_ID", nullable = false, unique = true, updatable = false, insertable = false)
    private Integer seqId;

    @Column(name = "C0001_CREATED_BY", nullable = false)
    private UUID createdBy;

    @Column(name = "C0001_UPDATED_BY", nullable = false)
    private UUID updatedBy;

    // Entity Fields
    @Column(name = "C0001_NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "C0001_EMAIL", nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "C0001_PHONE", length = 20)
    private String phone;

    @Column(name = "C0001_LOCALE", length = 5)
    private String locale;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.active == null) {
            this.active = true;
        }
        if (this.locale == null || this.locale.isEmpty()) {
            this.locale = "pt";
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
