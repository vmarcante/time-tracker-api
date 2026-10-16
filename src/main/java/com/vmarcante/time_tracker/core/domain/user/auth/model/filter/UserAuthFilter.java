package com.vmarcante.time_tracker.core.domain.user.auth.model.filter;

import java.util.Set;

import com.vmarcante.time_tracker.core.domain.user.enums.UserRoleType;

import lombok.Data;

@Data
public class UserAuthFilter {

    private String username;
    private Boolean active;
    private UserRoleType role;
    private Set<UserRoleType> roles;
}
