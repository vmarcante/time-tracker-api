package com.vmarcante.time_tracker.core.infraestructure.team.persistence;

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
@Table(name = "TB0006_TEAM", schema = "dbo", indexes = {
        @Index(name = "IX0006_SQ_ID", columnList = "C0006_SQ_ID", unique = true),
        @Index(name = "IX0006_COMPANY_ID", columnList = "C0006_COMPANY_ID"),
        @Index(name = "IX0006_NAME", columnList = "C0006_NAME"),
        @Index(name = "IX0006_ACTIVE", columnList = "C0006_ACTIVE"),
        @Index(name = "IX0006_CREATED_AT", columnList = "C0006_CREATED_AT")
})
@Data
public class TeamJpaEntity implements BaseActiveEntityInterface<UUID, Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "C0006_TEAM_ID")
    private UUID id;

    @Column(name = "C0006_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "C0006_UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "C0006_ACTIVE", nullable = false)
    private Boolean active;

    @Column(name = "C0006_SQ_ID", nullable = false, updatable = false, insertable = false)
    private Integer seqId;

    @Column(name = "C0006_COMPANY_ID", nullable = false)
    private UUID companyId;

    @Column(name = "C0006_NAME", nullable = false, length = 200)
    private String name;

    @Column(name = "C0006_DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Column(name = "C0006_CREATED_BY", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "C0006_UPDATED_BY", nullable = false)
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
