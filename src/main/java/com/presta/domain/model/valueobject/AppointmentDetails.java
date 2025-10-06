package com.presta.domain.model.valueobject;

public record AppointmentDetails(
        String reason,
        String notes
) {
    public AppointmentDetails {
        // Pas de validation stricte, ces champs sont optionnels
    }

    public AppointmentDetails addNote(String additionalNote) {
        if (additionalNote == null || additionalNote.isBlank()) {
            return this;
        }

        String updatedNotes = notes != null
                ? notes + " | " + additionalNote
                : additionalNote;

        return new AppointmentDetails(reason, updatedNotes);
    }
}