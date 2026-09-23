package com.vmarcante.time_tracker.core.domain.user.auth.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.base.domain.model.BaseDomainModel;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.user.enums.AffiliationStatus;
import com.vmarcante.time_tracker.core.domain.user.enums.UserRoleType;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserAuth extends BaseDomainModel<UUID, Integer> {
    private String username;
    private String password;
    private String passwordSalt;
    private String accessToken;
    private Boolean active;
    private Boolean userConfirmed;
    private Integer failedAttempts;
    private LocalDateTime lastPasswordChange;
    private Boolean passwordResetRequested;
    private LocalDateTime lastPasswordResetRequest;
    private LocalDateTime lastLogin;
    private UserRoleType role;
    private AffiliationStatus affiliation;
    private transient Person person;
}
