package com.presta.domain.model;

import com.presta.domain.model.valueobject.AppointmentDetails;
import com.presta.domain.model.valueobject.AppointmentStatus;
import com.presta.domain.model.valueobject.TimeSlot;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class Appointment {
    private final UUID id;
    private final UUID clientId;
    private final UUID contractorId;
    private final TimeSlot slot;
    private AppointmentStatus status;
    private AppointmentDetails details;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Factory method pour création
    public static Appointment create(
            UUID id,
            UUID clientId,
            UUID contractorId,
            LocalDateTime startDateTime,
            int duration,
            String reason,
            String notes
    ) {

        return new Appointment(
                id,
                clientId,
                contractorId,
                new TimeSlot(startDateTime, duration),
                AppointmentStatus.PENDING,
                new AppointmentDetails(reason, notes),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }


    public Appointment(
            UUID id,
            UUID clientId,
            UUID contractorId,
            TimeSlot slot,
            AppointmentStatus status,
            AppointmentDetails details,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        validateAppointment(clientId, contractorId, slot);

        this.id = id;
        this.clientId = clientId;
        this.contractorId = contractorId;
        this.slot = slot;
        this.status = status != null ? status : AppointmentStatus.PENDING;
        this.details = details != null ? details : new AppointmentDetails(null, null);
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    // Business Methods

    /**
     * Confirme le rendez-vous (action du prestataire)
     */
    public void confirm() {
        if (!status.canBeConfirmed()) {
            throw new IllegalStateException(
                    String.format("Impossible de confirmer un RDV avec le statut %s", status)
            );
        }

        this.status = AppointmentStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Annule le rendez-vous
     */
    public void cancel(String cancellationReason) {
        if (!status.canBeCancelled()) {
            throw new IllegalStateException(
                    String.format("Impossible d'annuler un RDV avec le statut %s", status)
            );
        }

        if (!slot.isInMoreThan24h()) {
            throw new IllegalStateException(
                    "Impossible d'annuler un RDV  à moins de 24h"
            );
        }

        this.status = AppointmentStatus.CANCELLED;
        this.details = details.addNote("Annulation: " + cancellationReason);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marque le rendez-vous comme terminé
     */
    public void complete() {
        if (!status.canBeCompleted()) {
            throw new IllegalStateException(
                    String.format("Impossible de terminer un RDV avec le statut %s", status)
            );
        }

        if (slot.isInFuture()) {
            throw new IllegalStateException(
                    "Impossible de terminer un RDV qui n'a pas encore eu lieu"
            );
        }

        this.status = AppointmentStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Ajoute une note au rendez-vous
     */
    public void addNote(String note) {
        this.details = details.addNote(note);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Vérifie si le RDV peut encore être modifié (non passé et actif)
     */
    public boolean isModifiable() {
        return slot.isInFuture() && status.isActive() && slot.isInMoreThan24h();
    }

    /**
     * Vérifie si ce RDV bloque un créneau donné
     */
    public boolean blocksTimeSlot(LocalDateTime start, int duration) {
        // Un RDV bloque un créneau s'il est actif et qu'il chevauche
        return status.isActive() && slot.overlaps(start, duration);
    }

    /**
     * Vérifie si ce RDV est pour un contractor spécifique à une date/heure
     */
    public boolean isAt(UUID contractorId, LocalDateTime dateTime) {
        return this.contractorId.equals(contractorId) &&
                this.slot.startDateTime().equals(dateTime);
    }

    // Validation
    private void validateAppointment( UUID clientId, UUID contractorId, TimeSlot slot) {

        if (clientId == null) {
            throw new IllegalArgumentException("L'ID du client est obligatoire");
        }

        if (contractorId == null) {
            throw new IllegalArgumentException("L'ID du prestataire est obligatoire");
        }

        if (slot == null) {
            throw new IllegalArgumentException("Le créneau est obligatoire");
        }
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getClientId() { return clientId; }
    public UUID getContractorId() { return contractorId; }
    public TimeSlot getSlot() { return slot; }
    public LocalDateTime getAppointmentDateTime() { return slot.startDateTime(); }
    public int getDuration() { return slot.duration(); }
    public LocalDateTime getEndDateTime() { return slot.getEndDateTime(); }
    public AppointmentStatus getStatus() { return status; }
    public AppointmentDetails getDetails() { return details; }
    public String getReason() { return details.reason(); }
    public String getNotes() { return details.notes(); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}