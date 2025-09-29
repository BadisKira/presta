package com.presta.application.usecases;

import com.presta.domain.model.valueobject.AvailableSlot;
import com.presta.domain.model.valueobject.AvailabilityStatus;
import com.presta.domain.model.valueobject.TimeSlot;
import com.presta.domain.port.in.SlotGenerationUseCasePort;
import com.presta.domain.port.out.SlotGeneratorPort;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * UseCase pour la génération et consultation des créneaux
 * Utilise le port SlotGeneratorPort (implémenté par SlotGeneratorAdapter)
 * Ne dépend PAS directement du domain service
 */
@Component
public class SlotGenerationUseCase implements SlotGenerationUseCasePort {
    @Override
    public List<AvailableSlot> generateAvailableSlots(GenerateAvailableSlotsCommand command) {
        return List.of();
    }

    @Override
    public List<TimeSlot> findOnlyAvailableSlots(AvailableSlotsQuery query) {
        return List.of();
    }

    @Override
    public boolean isSlotAvailable(CheckSlotAvailabilityCommand command) {
        return false;
    }
}
//public class SlotGenerationUseCase {
//
//    private final SlotGeneratorPort slotGeneratorPort;
//
//    public SlotGenerationUseCase(SlotGeneratorPort slotGeneratorPort) {
//        this.slotGeneratorPort = slotGeneratorPort;
//    }
//
//    /**
//     * Récupère tous les créneaux d'un contractor avec leur statut
//     * Pour affichage du calendrier complet
//     */
//    public List<AvailableSlot> getContractorSlots(
//            UUID contractorId,
//            LocalDate startDate,
//            LocalDate endDate) {
//
//        return slotGeneratorPort.generateAvailableSlots(
//                contractorId,
//                startDate,
//                endDate
//        );
//    }
//
//    /**
//     * Récupère uniquement les créneaux disponibles pour réservation
//     * Pour proposer les créneaux libres au client
//     */
//    public List<TimeSlot> getAvailableSlotsForBooking(
//            UUID contractorId,
//            LocalDate startDate,
//            LocalDate endDate) {
//
//        return slotGeneratorPort.findOnlyAvailableSlots(
//                contractorId,
//                startDate,
//                endDate
//        );
//    }
//
//    /**
//     * Vérifie si un créneau spécifique est disponible
//     * Pour validation avant réservation
//     */
//    public boolean checkSlotAvailability(
//            UUID contractorId,
//            LocalDateTime startDateTime,
//            int durationMinutes) {
//
//        return slotGeneratorPort.isSlotAvailable(
//                contractorId,
//                startDateTime,
//                durationMinutes
//        );
//    }
//
//    /**
//     * Trouve le prochain créneau disponible
//     */
//    public TimeSlot findNextAvailableSlot(
//            UUID contractorId,
//            LocalDateTime fromDateTime,
//            int durationMinutes) {
//
//        // Rechercher sur les 30 prochains jours
//        LocalDate startDate = fromDateTime.toLocalDate();
//        LocalDate endDate = startDate.plusDays(30);
//
//        List<TimeSlot> availableSlots = slotGeneratorPort
//                .findOnlyAvailableSlots(contractorId, startDate, endDate);
//
//        return availableSlots.stream()
//                .filter(slot ->
//                        slot.startDateTime().isAfter(fromDateTime) &&
//                                slot.duration() >= durationMinutes
//                )
//                .findFirst()
//                .orElse(null);
//    }
//
//    /**
//     * Obtient un résumé de disponibilité pour un mois
//     */
//    public SlotAvailabilitySummary getMonthlyAvailability(
//            UUID contractorId,
//            LocalDate month) {
//
//        LocalDate startOfMonth = month.withDayOfMonth(1);
//        LocalDate endOfMonth = month.withDayOfMonth(month.lengthOfMonth());
//
//        List<AvailableSlot> slots = slotGeneratorPort
//                .generateAvailableSlots(contractorId, startOfMonth, endOfMonth);
//
//        return calculateSummary(contractorId, month, slots);
//    }
//
//    /**
//     * Recherche des créneaux selon critères
//     */
//    public List<AvailableSlot> searchSlots(SlotSearchCriteria criteria) {
//        List<AvailableSlot> slots = slotGeneratorPort.generateAvailableSlots(
//                criteria.contractorId(),
//                criteria.startDate(),
//                criteria.endDate()
//        );
//
//        // Appliquer les filtres additionnels
//        return slots.stream()
//                .filter(slot -> matchesCriteria(slot, criteria))
//                .collect(Collectors.toList());
//    }
//
//    // ========== MÉTHODES PRIVÉES ==========
//
//    private SlotAvailabilitySummary calculateSummary(
//            UUID contractorId,
//            LocalDate month,
//            List<AvailableSlot> slots) {
//
//        int totalSlots = slots.size();
//        int availableSlots = (int) slots.stream()
//                .filter(s -> s.status() == AvailabilityStatus.AVAILABLE)
//                .count();
//        int bookedSlots = (int) slots.stream()
//                .filter(s -> s.status() == AvailabilityStatus.BOOKED)
//                .count();
//        int unavailableSlots = (int) slots.stream()
//                .filter(s -> s.status() == AvailabilityStatus.UNAVAILABLE)
//                .count();
//
//        double availabilityRate = totalSlots > 0 ?
//                (double) availableSlots / totalSlots * 100 : 0;
//
//        return new SlotAvailabilitySummary(
//                contractorId,
//                month,
//                totalSlots,
//                availableSlots,
//                bookedSlots,
//                unavailableSlots,
//                availabilityRate
//        );
//    }
//
//    private boolean matchesCriteria(AvailableSlot slot, SlotSearchCriteria criteria) {
//        // Filtrer par durée minimale
//        if (criteria.minDuration() != null &&
//                slot.timeSlot().duration() < criteria.minDuration()) {
//            return false;
//        }
//
//        // Filtrer par statut
//        if (criteria.status() != null &&
//                slot.status() != criteria.status()) {
//            return false;
//        }
//
//        // Filtrer par heure préférée
//        if (criteria.preferredHour() != null) {
//            int slotHour = slot.timeSlot().startDateTime().getHour();
//            if (Math.abs(slotHour - criteria.preferredHour()) > 2) {
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//    // ========== RECORDS POUR LES DONNÉES ==========
//
//    /**
//     * Critères de recherche
//     */
//    public record SlotSearchCriteria(
//            UUID contractorId,
//            LocalDate startDate,
//            LocalDate endDate,
//            Integer minDuration,
//            Integer preferredHour,
//            AvailabilityStatus status
//    ) {}
//
//    /**
//     * Résumé de disponibilité
//     */
//    public record SlotAvailabilitySummary(
//            UUID contractorId,
//            LocalDate month,
//            int totalSlots,
//            int availableSlots,
//            int bookedSlots,
//            int unavailableSlots,
//            double availabilityRate
//    ) {}
//}