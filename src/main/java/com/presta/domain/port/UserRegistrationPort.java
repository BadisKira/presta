package com.presta.domain.port;

import com.presta.domain.model.valueobject.ContactInfo;
import com.presta.domain.model.valueobject.KeycloakUserId;
import com.presta.domain.model.valueobject.UserProfile;

public interface UserRegistrationPort {
    enum UserRole {
        CLIENT, CONTRACTOR
    }

    void registerUser(KeycloakUserId keycloakId, UserProfile profile, ContactInfo contact, UserRole role);

}
