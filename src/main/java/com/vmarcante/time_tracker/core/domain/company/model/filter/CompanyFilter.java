package com.vmarcante.time_tracker.core.domain.company.model.filter;

import java.util.Collection;
import java.util.UUID;

import lombok.Data;

@Data
public class CompanyFilter {
    private UUID id;
    private Collection<UUID> ids;
    private String document;
    private String name;
    private Boolean active;
    private UUID memberUserId;
}
