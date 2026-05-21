package com.vmarcante.time_tracker.core.domain.user.auth.services;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.domain.exception.DomainException;
import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuthCredentials;

@Service
public class GenerateUserAuthCredentialsService {

    private final GeneratePasswordSaltService generatePasswordSaltService;
    private final GeneratePasswordHashService generatePasswordHashService;
    private final GenerateRandomAccessTokenService generateRandomAccessTokenService;

    public GenerateUserAuthCredentialsService(
            GeneratePasswordSaltService generatePasswordSaltService,
            GeneratePasswordHashService generatePasswordHashService,
            GenerateRandomAccessTokenService generateRandomAccessTokenService) {
        this.generatePasswordSaltService = generatePasswordSaltService;
        this.generatePasswordHashService = generatePasswordHashService;
        this.generateRandomAccessTokenService = generateRandomAccessTokenService;
    }

    public UserAuthCredentials execute(String password) throws DomainException {
        String salt = generatePasswordSaltService.execute();
        String passwordHash = generatePasswordHashService.execute(password, salt);
        String firstAccessToken = generateRandomAccessTokenService.execute();

        UserAuthCredentials credentials = new UserAuthCredentials();
        credentials.setPassword(password);
        credentials.setPasswordSalt(salt);
        credentials.setPasswordHash(passwordHash);
        credentials.setFirstAccessToken(firstAccessToken);

        return credentials;
    }
}
