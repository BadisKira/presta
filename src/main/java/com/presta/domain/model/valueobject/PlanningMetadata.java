package com.presta.domain.model.valueobject;

import java.time.LocalDateTime;
import java.util.Optional;

public record PlanningMetadata(
        int totalSlots,
        int availableCount,
        int bookedCount,
        int unavailableCount,
        Optional<TimeSlot> nextAvailable,
        LocalDateTime generatedAt
) {

    public double availabilityRate() {
        return totalSlots > 0 ? (double) availableCount / totalSlots * 100 : 0;
    }


    public double occupancyRate() {
        int bookableSlots = availableCount + bookedCount;
        return bookableSlots > 0 ? (double) bookedCount / bookableSlots * 100 : 0;
    }


    public boolean hasAvailability() {
        return availableCount > 0;
    }
}
