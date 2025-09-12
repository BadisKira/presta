package com.presta.domain.model;

import com.presta.domain.model.valueobject.Address;

import java.util.Objects;
import java.util.UUID;

public record Contractor(
        UUID id,
        String fullName,
        Address address,
        UUID assignmentId,
        String speciality
) {
    public Contractor {
        Objects.requireNonNull(id, "Contractor ID cannot be null");
        if (fullName != null && fullName.length() > 150) {
            throw new IllegalArgumentException("Full name cannot exceed 150 characters");
        }
        if (speciality != null && speciality.length() > 100) {
            throw new IllegalArgumentException("Speciality cannot exceed 100 characters");
        }
    }

    public static Contractor create(UUID userId, String fullName, Address address,
                                    UUID assignmentId, String speciality) {
        return new Contractor(userId, fullName, address, assignmentId, speciality);
    }


    public Contractor updateFullName(String newFullName) {
        return new Contractor(id, newFullName, address, assignmentId, speciality);
    }

    public Contractor updateAddress(Address newAddress) {
        return new Contractor(id, fullName, newAddress, assignmentId, speciality);
    }

    public Contractor updateAssignment(UUID newAssignmentId) {
        return new Contractor(id, fullName, address, newAssignmentId, speciality);
    }

    public Contractor updateSpeciality(String newSpeciality) {
        return new Contractor(id, fullName, address, assignmentId, newSpeciality);
    }
}
