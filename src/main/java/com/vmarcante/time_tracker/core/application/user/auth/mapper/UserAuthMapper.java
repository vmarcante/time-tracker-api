package com.vmarcante.time_tracker.core.application.user.auth.mapper;

import com.vmarcante.time_tracker.core.application.user.auth.dto.CreateUserAuthDTO;
import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;
import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuthCredentials;
import com.vmarcante.time_tracker.core.domain.user.enums.UserRoleType;

public class UserAuthMapper {

    public static UserAuth inputToModel(CreateUserAuthDTO input, UserAuthCredentials credentials) {
        UserAuth userAuth = new UserAuth();
        userAuth.setId(input.personId());
        userAuth.setUsername(input.username());
        userAuth.setPassword(credentials.getPasswordHash());
        userAuth.setPasswordSalt(credentials.getPasswordSalt());
        userAuth.setActive(true);
        userAuth.setFailedAttempts(0);
        userAuth.setAccessToken(credentials.getFirstAccessToken());
        userAuth.setRole(UserRoleType.USER);
        return userAuth;
    }
}
