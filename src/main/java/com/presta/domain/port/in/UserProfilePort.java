package com.presta.domain.port.in;

import com.presta.domain.model.Client;
import com.presta.domain.model.Contractor;
import com.presta.domain.model.User;
import com.presta.domain.model.valueobject.ContactInfo;
import com.presta.domain.model.valueobject.KeycloakUserId;
import com.presta.domain.model.valueobject.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserProfilePort {
    Optional<User> getUserProfile(KeycloakUserId keycloakId);
    void updateUserProfile(KeycloakUserId keycloakId, UserProfile profile, ContactInfo contact);

}