package com.presta.domain.newshit;

import com.presta.domain.model.valueobject.AvailabilityStatus;
import com.presta.domain.model.valueobject.AvailableSlot;
import com.presta.domain.model.valueobject.TimeSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Représentation complète du planning d'un contractor
 * Contient toutes les informations nécessaires pour afficher un planning
 */
public record ContractorPlanning(
        UUID contractorId,                    // ID du contractor
        LocalDate startDate,                  // Date de début du planning
        LocalDate endDate,                    // Date de fin du planning
        List<AvailableSlot> slots,           // Tous les créneaux avec leur statut
        PlanningMetadata metadata            // Métadonnées du planning
) {
    /**
     * Retourne uniquement les créneaux disponibles
     */
    public List<TimeSlot> getAvailableSlots() {
        return slots.stream()
                .filter(s -> s.status() == AvailabilityStatus.AVAILABLE)
                .map(AvailableSlot::timeSlot)
                .toList();
    }

    /**
     * Retourne uniquement les créneaux réservés
     */
    public List<TimeSlot> getBookedSlots() {
        return slots.stream()
                .filter(s -> s.status() == AvailabilityStatus.BOOKED)
                .map(AvailableSlot::timeSlot)
                .toList();
    }

    /**
     * Compte le nombre de créneaux par statut
     */
    public int countByStatus(AvailabilityStatus status) {
        return (int) slots.stream()
                .filter(s -> s.status() == status)
                .count();
    }
}

/**
 * Métadonnées du planning pour faciliter l'affichage et l'analyse
 */
record PlanningMetadata(
        int totalSlots,                      // Nombre total de créneaux générés
        int availableCount,                  // Nombre de créneaux disponibles
        int bookedCount,                     // Nombre de créneaux réservés
        int unavailableCount,                // Nombre de créneaux indisponibles
        Optional<TimeSlot> nextAvailable,    // Prochain créneau disponible (si existe)
        LocalDateTime generatedAt            // Date/heure de génération du planning
) {
    /**
     * Calcule le taux de disponibilité
     */
    public double availabilityRate() {
        return totalSlots > 0 ? (double) availableCount / totalSlots * 100 : 0;
    }

    /**
     * Calcule le taux d'occupation (sur les créneaux réservables)
     */
    public double occupancyRate() {
        int bookableSlots = availableCount + bookedCount;
        return bookableSlots > 0 ? (double) bookedCount / bookableSlots * 100 : 0;
    }

    /**
     * Indique si le planning a des disponibilités
     */
    public boolean hasAvailability() {
        return availableCount > 0;
    }
}
