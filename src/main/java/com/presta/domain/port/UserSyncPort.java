package com.presta.domain.port;

import com.presta.domain.model.valueobject.ContactInfo;
import com.presta.domain.model.valueobject.KeycloakUserId;
import com.presta.domain.model.valueobject.UserProfile;

public interface UserSyncPort {

        enum UserRole {
            CLIENT, CONTRACTOR
        }

        void syncUser(KeycloakUserId keycloakUserId,UserProfile profile, ContactInfo contact, UserRole role);



}
