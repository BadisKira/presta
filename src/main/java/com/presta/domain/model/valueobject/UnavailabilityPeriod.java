package com.presta.domain.model.valueobject;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Value Object - Période d'indisponibilité
 * Représente la période exacte où le prestataire n'est pas disponible
 */
public record UnavailabilityPeriod(
        LocalDate startDate,
        LocalDate endDate,
        LocalTime startTime,
        LocalTime endTime
) {

    public UnavailabilityPeriod {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Les dates de début et fin sont obligatoires");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("La date de fin doit être après ou égale à la date de début");
        }


        if ((startTime == null && endTime != null) || (startTime != null && endTime == null)) {
            throw new IllegalArgumentException(
                    "Les heures de début et fin doivent être toutes les deux définies ou toutes les deux nulles"
            );
        }

        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("L'heure de fin doit être après l'heure de début");
        }
    }

    /**
     * Vérifie si cette période est une journée complète
     */
    public boolean isFullDay() {
        return startTime == null && endTime == null;
    }

    /**
     * Vérifie si une date/heure spécifique tombe dans cette période d'indisponibilité
     */
    public boolean contains(LocalDateTime dateTime) {
        LocalDate date = dateTime.toLocalDate();

        // Vérifier si la date est dans la période
        if (date.isBefore(startDate) || date.isAfter(endDate)) {
            return false;
        }

        // Si journée complète, tout moment de ces jours est indisponible
        if (isFullDay()) {
            return true;
        }

        // Sinon, vérifier l'heure
        LocalTime time = dateTime.toLocalTime();
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    /**
     * Vérifie si cette période chevauche avec un créneau
     */
    public boolean overlapsWithSlot(LocalDateTime slotStart, LocalDateTime slotEnd) {
        // Si le slot est complètement avant ou après la période
        if (slotEnd.toLocalDate().isBefore(startDate) ||
                slotStart.toLocalDate().isAfter(endDate)) {
            return false;
        }

        // Si journée complète, vérifier si le slot tombe dans les jours
        if (isFullDay()) {
            return !slotEnd.toLocalDate().isBefore(startDate) &&
                    !slotStart.toLocalDate().isAfter(endDate);
        }

        // Vérification plus fine avec les heures
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDateTime periodStart = LocalDateTime.of(date, startTime);
            LocalDateTime periodEnd = LocalDateTime.of(date, endTime);

            // Vérifier le chevauchement
            if (!slotEnd.isBefore(periodStart) && !slotStart.isAfter(periodEnd)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Retourne une représentation textuelle de la période
     */
    public String toDisplayString() {
        if (startDate.equals(endDate)) {
            if (isFullDay()) {
                return String.format("Le %s (journée complète)", startDate);
            } else {
                return String.format("Le %s de %s à %s", startDate, startTime, endTime);
            }
        } else {
            if (isFullDay()) {
                return String.format("Du %s au %s (journées complètes)", startDate, endDate);
            } else {
                return String.format("Du %s au %s, de %s à %s", startDate, endDate, startTime, endTime);
            }
        }
    }
}