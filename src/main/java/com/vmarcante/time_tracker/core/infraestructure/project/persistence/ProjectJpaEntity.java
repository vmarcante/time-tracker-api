package com.vmarcante.time_tracker.core.infraestructure.project.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.base.infraestructure.persistence.BaseActiveEntityInterface;
import com.vmarcante.time_tracker.core.domain.project.enums.ProjectStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "TB0007_PROJECT", schema = "dbo", indexes = {
        @Index(name = "IX0007_SQ_ID", columnList = "C0007_SQ_ID", unique = true),
        @Index(name = "IX0007_COMPANY_ID", columnList = "C0007_COMPANY_ID"),
        @Index(name = "IX0007_NAME", columnList = "C0007_NAME"),
        @Index(name = "IX0007_STATUS", columnList = "C0007_STATUS"),
        @Index(name = "IX0007_ACTIVE", columnList = "C0007_ACTIVE"),
        @Index(name = "IX0007_CREATED_AT", columnList = "C0007_CREATED_AT")
})
@Data
public class ProjectJpaEntity implements BaseActiveEntityInterface<UUID, Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "C0007_PROJECT_ID")
    private UUID id;

    @Column(name = "C0007_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "C0007_UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "C0007_ACTIVE", nullable = false)
    private Boolean active;

    @Column(name = "C0007_SQ_ID", nullable = false, updatable = false, insertable = false)
    private Integer seqId;

    @Column(name = "C0007_COMPANY_ID", nullable = false)
    private UUID companyId;

    @Column(name = "C0007_NAME", nullable = false, length = 200)
    private String name;

    @Column(name = "C0007_DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "C0007_STATUS", nullable = false, length = 20)
    private ProjectStatus status;

    @Column(name = "C0007_START_DATE")
    private LocalDate startDate;

    @Column(name = "C0007_END_DATE")
    private LocalDate endDate;

    @Column(name = "C0007_CREATED_BY", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "C0007_UPDATED_BY", nullable = false)
    private UUID updatedBy;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.active == null) {
            this.active = true;
        }
        if (this.status == null) {
            this.status = ProjectStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
