package com.vmarcante.time_tracker.core.domain.user.auth.model;

import lombok.Data;

@Data
public class UserAuthCredentials {
    private String password;
    private String passwordSalt;
    private String passwordHash;
    private String firstAccessToken;
}
