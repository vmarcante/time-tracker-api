package com.vmarcante.time_tracker.core.domain.person.model.filter;

import java.util.UUID;

import lombok.Data;

@Data
public class PersonFilter {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private Boolean active;
    private String nameContains;
    private String emailContains;
}
