package com.vmarcante.time_tracker.core.infraestructure.team.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.base.infraestructure.persistence.BaseActiveEntityInterface;
import com.vmarcante.time_tracker.core.domain.team.enums.TeamRole;

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
@Table(name = "TB0010_USER_TEAM", schema = "dbo", indexes = {
        @Index(name = "IX0010_SQ_ID", columnList = "C0010_SQ_ID", unique = true),
        @Index(name = "IX0010_USER_ID", columnList = "C0010_USER_ID"),
        @Index(name = "IX0010_TEAM_ID", columnList = "C0010_TEAM_ID"),
        @Index(name = "IX0010_ROLE", columnList = "C0010_ROLE"),
        @Index(name = "IX0010_ACTIVE", columnList = "C0010_ACTIVE"),
        @Index(name = "IX0010_CREATED_AT", columnList = "C0010_CREATED_AT")
})
@Data
public class UserTeamJpaEntity implements BaseActiveEntityInterface<UUID, Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "C0010_ID")
    private UUID id;

    @Column(name = "C0010_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "C0010_UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "C0010_ACTIVE", nullable = false)
    private Boolean active;

    @Column(name = "C0010_SQ_ID", nullable = false, updatable = false, insertable = false)
    private Integer seqId;

    @Column(name = "C0010_USER_ID", nullable = false)
    private UUID userId;

    @Column(name = "C0010_TEAM_ID", nullable = false)
    private UUID teamId;

    @Enumerated(EnumType.STRING)
    @Column(name = "C0010_ROLE", nullable = false, length = 50)
    private TeamRole role;

    @Column(name = "C0010_APPROVED", nullable = false)
    private Boolean approved;

    @Column(name = "C0010_CREATED_BY", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "C0010_UPDATED_BY", nullable = false)
    private UUID updatedBy;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.active == null) {
            this.active = true;
        }
        if (this.approved == null) {
            this.approved = false;
        }
        if (this.role == null) {
            this.role = TeamRole.MEMBER;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
