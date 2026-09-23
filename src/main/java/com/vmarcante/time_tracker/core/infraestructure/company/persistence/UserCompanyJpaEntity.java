package com.vmarcante.time_tracker.core.infraestructure.company.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.base.infraestructure.persistence.BaseActiveEntityInterface;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.enums.MembershipOrigin;

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
@Table(name = "TB0009_USER_COMPANY", schema = "dbo", indexes = {
        @Index(name = "IX0009_SQ_ID", columnList = "C0009_SQ_ID", unique = true),
        @Index(name = "IX0009_USER_ID", columnList = "C0009_USER_ID"),
        @Index(name = "IX0009_COMPANY_ID", columnList = "C0009_COMPANY_ID"),
        @Index(name = "IX0009_ROLE", columnList = "C0009_ROLE"),
        @Index(name = "IX0009_ACTIVE", columnList = "C0009_ACTIVE")
})
@Data
public class UserCompanyJpaEntity implements BaseActiveEntityInterface<UUID, Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "C0009_ID")
    private UUID id;

    @Column(name = "C0009_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "C0009_UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "C0009_ACTIVE", nullable = false)
    private Boolean active;

    @Column(name = "C0009_SQ_ID", nullable = false, updatable = false, insertable = false)
    private Integer seqId;

    @Column(name = "C0009_USER_ID", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "C0009_COMPANY_ID", nullable = false, updatable = false)
    private UUID companyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "C0009_ROLE", nullable = false, length = 50)
    private CompanyRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "C0009_ORIGIN", nullable = false, length = 10)
    private MembershipOrigin origin;

    @Column(name = "C0009_APPROVED", nullable = false)
    private Boolean approved;

    @Column(name = "C0009_APPROVED_AT")
    private LocalDateTime approvedAt;

    @Column(name = "C0009_APPROVED_BY")
    private UUID approvedBy;

    @Column(name = "C0009_CREATED_BY", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "C0009_UPDATED_BY", nullable = false)
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
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
