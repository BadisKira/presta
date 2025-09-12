package com.presta.domain.port.out;

import com.presta.domain.model.valueobject.ContactInfo;
import com.presta.domain.model.valueobject.KeycloakUserId;
import com.presta.domain.model.valueobject.UserProfile;

import java.util.List;
import java.util.Optional;

public interface UserAuthentificationPort {

    record AuthenticatedUser(
            KeycloakUserId keycloakId,
            UserProfile profile,
            ContactInfo contact,
            List<String> roles
    ) {}

    Optional<AuthenticatedUser> getCurrentAuthenticatedUser();
    List<String> getUserRoles(KeycloakUserId keycloakId);
}
