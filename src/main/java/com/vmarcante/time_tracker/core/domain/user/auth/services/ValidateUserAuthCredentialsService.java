package com.vmarcante.time_tracker.core.domain.user.auth.services;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;
import com.vmarcante.time_tracker.core.domain.user.auth.port.PasswordEncryptionPort;
import com.vmarcante.time_tracker.core.shared.utils.StringValidationUtils;

@Service
public class ValidateUserAuthCredentialsService {

    private final PasswordEncryptionPort passwordEncryption;
    
    public ValidateUserAuthCredentialsService(PasswordEncryptionPort passwordEncryption) {
        this.passwordEncryption = passwordEncryption;
    }

    public boolean execute(UserAuth userAuth, String incomingPassword) {

        if (userAuth == null || !StringValidationUtils.containsContent(incomingPassword)) {
            return false;
        }
        
        String incomingPasswordWithSalt = incomingPassword + userAuth.getPasswordSalt();
        return passwordEncryption.matches(incomingPasswordWithSalt, userAuth.getPassword());
    }
    
}
