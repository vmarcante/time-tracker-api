package com.vmarcante.time_tracker.core.infraestructure.timeentry.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.base.infraestructure.persistence.BaseActiveEntityInterface;

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
@Table(name = "TB0013_TIME_ENTRY_SESSION", schema = "dbo", indexes = {
        @Index(name = "IX0013_SQ_ID", columnList = "C0013_SQ_ID", unique = true),
        @Index(name = "IX0013_ENTRY_ID", columnList = "C0013_ENTRY_ID"),
        @Index(name = "IX0013_USER_START", columnList = "C0013_USER_ID, C0013_START_TIME"),
        @Index(name = "IX0013_ACTIVE", columnList = "C0013_ACTIVE"),
        @Index(name = "IX0013_CREATED_BY", columnList = "C0013_CREATED_BY")
})
@Data
public class TimeEntrySessionJpaEntity implements BaseActiveEntityInterface<UUID, Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "C0013_SESSION_ID")
    private UUID id;

    @Column(name = "C0013_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "C0013_UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "C0013_ACTIVE", nullable = false)
    private Boolean active;

    @Column(name = "C0013_SQ_ID", nullable = false, updatable = false, insertable = false)
    private Integer seqId;

    @Column(name = "C0013_ENTRY_ID", nullable = false)
    private UUID entryId;

    @Column(name = "C0013_USER_ID", nullable = false)
    private UUID userId;

    @Column(name = "C0013_START_TIME", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "C0013_END_TIME")
    private LocalDateTime endTime;

    @Column(name = "C0013_DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Column(name = "C0013_CREATED_BY", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "C0013_UPDATED_BY", nullable = false)
    private UUID updatedBy;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.active == null) {
            this.active = true;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
