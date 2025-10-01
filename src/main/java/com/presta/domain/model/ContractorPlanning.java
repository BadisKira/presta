package com.presta.domain.model;

import com.presta.domain.model.valueobject.AvailabilityStatus;
import com.presta.domain.model.valueobject.AvailableSlot;
import com.presta.domain.model.valueobject.PlanningMetadata;
import com.presta.domain.model.valueobject.TimeSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public record ContractorPlanning(
        UUID contractorId,
        LocalDate startDate,
        LocalDate endDate,
        List<AvailableSlot> slots,
        PlanningMetadata metadata
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


