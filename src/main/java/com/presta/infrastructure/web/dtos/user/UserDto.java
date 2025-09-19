package com.presta.infrastructure.web.dtos.user;

import com.presta.domain.model.User;
import com.presta.domain.model.valueobject.ContactInfo;
import com.presta.domain.model.valueobject.UserProfile;

import java.util.UUID;

public record UserDto(
        UUID id,
        UUID keycloakId,
        String firstName,
        String lastName,
        String email
) {

    public static UserDto fromDomain(User user) {
        UserProfile profile = user.profile();
        ContactInfo contact = user.contactInfo();
        return new UserDto(
                user.id(),
                user.keycloakId().getValue(),
                profile.getFirstName(),
                profile.getLastName(),
                contact.email()
        );
    }
}
