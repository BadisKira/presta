package com.presta.domain.model.valueobject;

public enum AvailabilityStatus {
    AVAILABLE("Disponible"),
    BOOKED("Réservé"),
    UNAVAILABLE("Indisponible"),
    PAST("Passé");

    private final String displayName;

    AvailabilityStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
