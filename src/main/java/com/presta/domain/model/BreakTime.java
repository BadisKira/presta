package com.presta.domain.model;

import com.presta.domain.model.valueobject.TimeRange;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

public record BreakTime(
        TimeRange timeRange,
        Set<DayOfWeek> weekDays  // null = tous les jours de la règle
) {
    public BreakTime {
        if (timeRange == null) {
            throw new IllegalArgumentException("La plage horaire de pause est obligatoire");
        }
        // weekDays peut être null (= pause tous les jours)
        if (weekDays != null && weekDays.isEmpty()) {
            throw new IllegalArgumentException("Si défini, au moins un jour doit être sélectionné");
        }
    }

    /**
     * Vérifie si cette pause s'applique à un jour donné
     */
    public boolean appliesTo(DayOfWeek day) {
        return weekDays == null || weekDays.contains(day);
    }

    /**
     * Vérifie si cette pause chevauche avec un créneau
     */
    public boolean overlapsWithSlot(LocalDateTime slotStart, LocalDateTime slotEnd) {
        LocalTime slotStartTime = slotStart.toLocalTime();
        LocalTime slotEndTime = slotEnd.toLocalTime();

        // Vérifier si la pause s'applique à ce jour
        if (!appliesTo(slotStart.getDayOfWeek())) {
            return false;
        }

        // Vérifier le chevauchement temporel
        return !slotEndTime.isBefore(timeRange.startTime()) &&
                !slotStartTime.isAfter(timeRange.endTime());
    }
}
