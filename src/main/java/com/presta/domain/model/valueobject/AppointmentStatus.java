package com.presta.domain.model.valueobject;

public enum AppointmentStatus {
    PENDING("En attente"),
    CONFIRMED("Confirmé"),
    CANCELLED("Annulé"),
    COMPLETED("Terminé");

    private final String displayName;

    AppointmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return this == PENDING || this == CONFIRMED;
    }

    public boolean canBeConfirmed() {
        return this == PENDING;
    }

    public boolean canBeCancelled() {
        return this == PENDING || this == CONFIRMED ;
    }

    public boolean canBeCompleted() {
        return this == CONFIRMED;
    }
}