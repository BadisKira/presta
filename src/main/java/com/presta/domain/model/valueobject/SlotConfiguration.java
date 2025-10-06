package com.presta.domain.model.valueobject;

public record SlotConfiguration(
        int slotDuration,  // durée d'un créneau en minutes
        int restTime       // temps de repos entre créneaux en minutes
) {
    public SlotConfiguration {
        if (slotDuration <= 0 || slotDuration > 480) {
            throw new IllegalArgumentException(
                    "La durée d'un créneau doit être entre 1 et 480 minutes"
            );
        }
        if (restTime < 0 || restTime > 120) {
            throw new IllegalArgumentException(
                    "Le temps de repos doit être entre 0 et 120 minutes"
            );
        }
    }

    public int getTotalSlotTime() {
        return slotDuration + restTime;
    }
}