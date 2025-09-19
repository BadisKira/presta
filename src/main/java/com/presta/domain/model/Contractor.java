package com.presta.domain.model;

import java.util.Objects;
import java.util.UUID;

public record Contractor(
        UUID id,
        User user,
        String fullName,
        String address,
        Assignment assignment,
        String speciality
) {
    public Contractor {
        Objects.requireNonNull(id, "Contractor ID cannot be null");
        Objects.requireNonNull(user, "Contractor must have a User");

        if (fullName != null && fullName.length() > 150) {
            throw new IllegalArgumentException("Full name cannot exceed 150 characters");
        }
        if (speciality != null && speciality.length() > 100) {
            throw new IllegalArgumentException("Speciality cannot exceed 100 characters");
        }
    }

    public static Contractor create(UUID id, User user, String fullName, String address,
                                    Assignment assignment, String speciality) {
        return new Contractor(id, user, fullName, address, assignment, speciality);
    }


    public Contractor chooseAssignment(Assignment assignment){
        return  new Contractor(
                this.id(),
                this.user(),
                this.fullName(),
                this.address(),
                assignment,
                this.speciality()
        );
    }
}