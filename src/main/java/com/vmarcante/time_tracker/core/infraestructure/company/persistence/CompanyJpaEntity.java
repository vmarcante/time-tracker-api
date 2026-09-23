package com.vmarcante.time_tracker.core.infraestructure.company.persistence;

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
@Table(name = "TB0005_COMPANY", schema = "dbo", indexes = {
        @Index(name = "IX0005_SQ_ID", columnList = "C0005_SQ_ID", unique = true),
        @Index(name = "IX0005_DOCUMENT", columnList = "C0005_DOCUMENT", unique = true),
        @Index(name = "IX0005_LEGAL_NAME", columnList = "C0005_LEGAL_NAME"),
        @Index(name = "IX0005_TRADE_NAME", columnList = "C0005_TRADE_NAME"),
        @Index(name = "IX0005_ACTIVE", columnList = "C0005_ACTIVE"),
        @Index(name = "IX0005_CREATED_AT", columnList = "C0005_CREATED_AT")
})
@Data
public class CompanyJpaEntity implements BaseActiveEntityInterface<UUID, Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "C0005_COMPANY_ID")
    private UUID id;

    @Column(name = "C0005_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "C0005_UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "C0005_ACTIVE", nullable = false)
    private Boolean active;

    @Column(name = "C0005_SQ_ID", nullable = false, updatable = false, insertable = false)
    private Integer seqId;

    @Column(name = "C0005_LEGAL_NAME", nullable = false, length = 200)
    private String legalName;

    @Column(name = "C0005_TRADE_NAME", length = 200)
    private String tradeName;

    @Column(name = "C0005_DOCUMENT", nullable = false, length = 18, unique = true)
    private String document;

    @Column(name = "C0005_DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Column(name = "C0005_TIMEZONE", nullable = false, length = 50)
    private String timezone;

    @Column(name = "C0005_CREATED_BY", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "C0005_UPDATED_BY", nullable = false)
    private UUID updatedBy;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.active == null) {
            this.active = true;
        }
        if (this.timezone == null) {
            this.timezone = "America/Sao_Paulo";
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
