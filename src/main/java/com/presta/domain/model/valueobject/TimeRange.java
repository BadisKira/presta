package com.presta.domain.model.valueobject;

import java.time.Duration;
import java.time.LocalTime;

public record TimeRange(
        LocalTime startTime,
        LocalTime endTime
) {
    // Validation dans le constructeur compact
    public TimeRange {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Les heures de début et fin sont obligatoires");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("L'heure de fin doit être après l'heure de début");
        }
    }

    public boolean contains(LocalTime time) {
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    public int getDurationInMinutes() {
        return (int) Duration.between(startTime, endTime).toMinutes();
    }

    public boolean overlaps(TimeRange other) {
        return !this.endTime.isBefore(other.startTime) &&
                !other.endTime.isBefore(this.startTime);
    }
}