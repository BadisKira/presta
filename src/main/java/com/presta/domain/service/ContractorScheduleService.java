package com.presta.domain.service;

import com.presta.domain.model.*;
import com.presta.domain.model.valueobject.*;
import com.presta.domain.port.ContractorSchedulePort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ContractorScheduleService  implements ContractorSchedulePort {
    @Override
    public ContractorPlanning generatePlanning(
            UUID contractorId,
            AvailabilityRule availabilityRule,
            List<UnavailabilityRule> unavailabilityRules,
            List<Appointment> existingAppointments,
            LocalDate startDate,
            LocalDate endDate) {

        // 1. Validation des paramètres obligatoires
        if (contractorId == null || availabilityRule == null ||
                startDate == null || endDate == null) {

            // Retourner un planning vide avec métadonnées par défaut
            return new ContractorPlanning(
                    contractorId,
                    startDate,
                    endDate,
                    Collections.emptyList(),
                    createEmptyMetadata()
            );
        }

        // 2. Générer les créneaux avec leur statut
        List<AvailableSlot> slots = generateSlots(
                contractorId,
                availabilityRule,
                unavailabilityRules,
                existingAppointments,
                startDate,
                endDate
        );

        // 3. Calculer les métadonnées du planning
        PlanningMetadata metadata = calculatePlanningMetadata(slots);

        // 4. Créer et retourner le planning complet
        return new ContractorPlanning(
                contractorId,
                startDate,
                endDate,
                slots,
                metadata
        );
    }

    /**
     * Calcule les métadonnées à partir des créneaux générés
     */
    private PlanningMetadata calculatePlanningMetadata(List<AvailableSlot> slots) {
        // Compter les créneaux par statut
        Map<AvailabilityStatus, Long> statusCounts = slots.stream()
                .collect(Collectors.groupingBy(
                        AvailableSlot::status,
                        Collectors.counting()
                ));

        int totalSlots = slots.size();
        int availableCount = statusCounts.getOrDefault(AvailabilityStatus.AVAILABLE, 0L).intValue();
        int bookedCount = statusCounts.getOrDefault(AvailabilityStatus.BOOKED, 0L).intValue();
        int unavailableCount = statusCounts.getOrDefault(AvailabilityStatus.UNAVAILABLE, 0L).intValue();

        // Trouver le prochain créneau disponible
        Optional<TimeSlot> nextAvailable = findNextAvailableSlotFromNow(slots);

        return new PlanningMetadata(
                totalSlots,
                availableCount,
                bookedCount,
                unavailableCount,
                nextAvailable,
                LocalDateTime.now()
        );
    }

    @Override
    public List<AvailableSlot> generateSlots(
            UUID contractorId,
            AvailabilityRule availabilityRule,
            List<UnavailabilityRule> unavailabilityRules,
            List<Appointment> appointments,
            LocalDate startDate,
            LocalDate endDate) {

        // 1. Validation des paramètres obligatoires
        if (contractorId == null || availabilityRule == null ||
                startDate == null || endDate == null) {
            return Collections.emptyList();
        }

        // 2. Vérifier la cohérence des dates
        if (startDate.isAfter(endDate)) {
            return Collections.emptyList();
        }

        // 3. Vérifier que la règle est active
        if (!availabilityRule.isActive()) {
            return Collections.emptyList();
        }

        // 4. Initialiser les listes pour éviter les NPE
        List<UnavailabilityRule> safeUnavailabilities =
                unavailabilityRules != null ? unavailabilityRules : Collections.emptyList();
        List<Appointment> safeAppointments =
                appointments != null ? appointments : Collections.emptyList();

        // 5. Générer les créneaux bruts pour chaque jour de la période
        List<TimeSlot> rawSlots = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            // Utiliser generateSlotsForDay qui gère déjà les BreakTimes
            List<TimeSlot> daySlots = availabilityRule.generateSlotsForDay(currentDate);
            rawSlots.addAll(daySlots);
            currentDate = currentDate.plusDays(1);
        }

        // 6. Trier les créneaux par ordre chronologique
        rawSlots.sort(Comparator.comparing(TimeSlot::startDateTime));

        // 7. Marquer chaque créneau avec son statut
        List<AvailableSlot> markedSlots = new ArrayList<>();
        for (TimeSlot slot : rawSlots) {
            AvailabilityStatus status = determineSlotStatus(
                    slot,
                    safeUnavailabilities,
                    safeAppointments
            );
            markedSlots.add(new AvailableSlot(contractorId, slot, status));
        }

        return markedSlots;
    }

    @Override
    public Optional<TimeSlot> findNextAvailableSlot(AvailabilityRule availabilityRule, List<UnavailabilityRule> unavailabilityRules, List<Appointment> appointments, LocalDateTime fromDateTime, int requiredDuration, int maxDaysToSearch) {
        return Optional.empty();
    }

    @Override
    public List<TimeSlot> findAvailableSlotsWithDuration(UUID contractorId, AvailabilityRule availabilityRule, List<UnavailabilityRule> unavailabilityRules, List<Appointment> appointments, LocalDate startDate, LocalDate endDate, int requiredDuration) {
        return List.of();
    }

    @Override
    public List<TimeSlot> extractAvailableSlots(List<AvailableSlot> slots) {
        return List.of();
    }

    @Override
    public List<TimeSlot> extractBookedSlots(List<AvailableSlot> slots) {
        return List.of();
    }

    @Override
    public List<TimeSlot> extractUnavailableSlots(List<AvailableSlot> slots) {
        return List.of();
    }

    @Override
    public List<TimeSlot> filterByStatus(List<AvailableSlot> slots, AvailabilityStatus status) {
        return List.of();
    }

    @Override
    public boolean isSlotAvailable(AvailabilityRule availabilityRule, List<UnavailabilityRule> unavailabilityRules, List<Appointment> appointments, TimeSlot requestedSlot) {
        return false;
    }

    @Override
    public boolean hasAvailability(AvailabilityRule availabilityRule, List<UnavailabilityRule> unavailabilityRules, List<Appointment> appointments, LocalDate startDate, LocalDate endDate) {
        return false;
    }

    /**
     * Détermine le statut d'un créneau en fonction des contraintes
     * Ordre de priorité : PAST > UNAVAILABLE > BOOKED > AVAILABLE
     */
    private AvailabilityStatus determineSlotStatus(
            TimeSlot slot,
            List<UnavailabilityRule> unavailabilities,
            List<Appointment> appointments) {

        // 1. Vérifier si le créneau est dans le passé
        if (slot.isInPast()) {
            return AvailabilityStatus.PAST;
        }

        // 2. Vérifier les indisponibilités (vacances, absences)
        for (UnavailabilityRule unavailability : unavailabilities) {
            if (unavailability.blocksTimeSlot(slot)) {
                return AvailabilityStatus.UNAVAILABLE;
            }
        }

        // 3. Vérifier les rendez-vous existants (seulement PENDING et CONFIRMED)
        for (Appointment appointment : appointments) {
            AppointmentStatus status = appointment.getStatus();
            boolean isActiveAppointment =
                    status == AppointmentStatus.PENDING ||
                            status == AppointmentStatus.CONFIRMED;

            if (isActiveAppointment && appointment.getSlot().overlaps(slot)) {
                return AvailabilityStatus.BOOKED;
            }
        }

        // 4. Si aucune contrainte, le créneau est disponible
        return AvailabilityStatus.AVAILABLE;
    }


    /**
     * Trouve le prochain créneau disponible à partir de maintenant
     */
    private Optional<TimeSlot> findNextAvailableSlotFromNow(List<AvailableSlot> slots) {
        LocalDateTime now = LocalDateTime.now();

        return slots.stream()
                .filter(slot -> slot.status() == AvailabilityStatus.AVAILABLE)
                .map(AvailableSlot::timeSlot)
                .filter(slot -> slot.startDateTime().isAfter(now))
                .min(Comparator.comparing(TimeSlot::startDateTime));
    }

    /**
     * Crée des métadonnées vides pour un planning invalide
     */
    private PlanningMetadata createEmptyMetadata() {
        return new PlanningMetadata(
                0,                      // totalSlots
                0,                      // availableCount
                0,                      // bookedCount
                0,                      // unavailableCount
                Optional.empty(),       // nextAvailable
                LocalDateTime.now()     // generatedAt
        );
    }

}
