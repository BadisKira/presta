package com.presta.domain.port.in;

import com.presta.domain.model.valueobject.ContactInfo;
import com.presta.domain.model.valueobject.KeycloakUserId;
import com.presta.domain.model.valueobject.UserProfile;

import java.util.List;

public interface UserSyncPort {

        enum UserRole {
            CLIENT, CONTRACTOR
        }

        void syncUser(UserProfile profile, ContactInfo contact, List<UserRole> roles);
        boolean userExists(KeycloakUserId keycloakId);

}
