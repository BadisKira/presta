package com.presta.domain.service;

import com.presta.domain.model.Appointment;
import com.presta.domain.model.AvailabilityRule;
import com.presta.domain.model.UnavailabilityRule;
import com.presta.domain.model.valueobject.AppointmentStatus;
import com.presta.domain.model.valueobject.AvailabilityStatus;
import com.presta.domain.model.valueobject.AvailableSlot;
import com.presta.domain.model.valueobject.TimeSlot;
import com.presta.domain.port.out.SlotGeneratorPort;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Domain Service PUR - Implémente toute la logique métier de génération des créneaux
 * Ne dépend d'AUCUN repository, travaille uniquement avec les entités du domaine
 */

public class SlotGeneratorService implements SlotGeneratorPort {

    // Configuration métier (peut être externalisée)
    private static final int MIN_SLOT_DURATION = 30; // minutes
    private static final int MAX_SLOT_DURATION = 240; // 4 heures
    private static final int MAX_BOOKING_ADVANCE_DAYS = 90; // 3 mois

    // ========== GÉNÉRATION DE CRÉNEAUX ==========

    @Override
    public List<TimeSlot> generateRawSlots(
            List<AvailabilityRule> availabilityRules,
            LocalDate startDate,
            LocalDate endDate) {

        if (availabilityRules == null || availabilityRules.isEmpty()) {
            return Collections.emptyList();
        }

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return Collections.emptyList();
        }

        List<TimeSlot> allSlots = new ArrayList<>();
        LocalDate currentDate = startDate;

        // Pour chaque jour de la période
        while (!currentDate.isAfter(endDate)) {
            LocalDate day = currentDate;

            // Pour chaque règle de disponibilité
            for (AvailabilityRule rule : availabilityRules) {
                if (rule.isActive()) {
                    // generateSlotsForDay gère déjà les BreakTime
                    List<TimeSlot> daySlots = rule.generateSlotsForDay(day);
                    allSlots.addAll(daySlots);
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        // Trier par ordre chronologique
        return sortSlotsByDateTime(allSlots);
    }

    // ========== MARQUAGE ET FILTRAGE ==========

    @Override
    public List<AvailableSlot> markSlotsAvailability(
            UUID contractorId,
            List<TimeSlot> rawSlots,
            List<UnavailabilityRule> unavailabilityRules,
            List<Appointment> existingAppointments) {

        if (contractorId == null || rawSlots == null || rawSlots.isEmpty()) {
            return Collections.emptyList();
        }

        // Préparer les listes pour éviter les NPE
        List<UnavailabilityRule> unavailabilities =
                unavailabilityRules != null ? unavailabilityRules : Collections.emptyList();

        List<Appointment> appointments =
                existingAppointments != null ? existingAppointments : Collections.emptyList();

        // Marquer chaque créneau avec son statut
        return rawSlots.stream()
                .map(slot -> {
                    AvailabilityStatus status = determineSlotStatus(slot, unavailabilities, appointments);
                    return new AvailableSlot(contractorId, slot, status);
                })
                .collect(Collectors.toList());
    }

    @Override
    public AvailabilityStatus determineSlotStatus(
            TimeSlot slot,
            List<UnavailabilityRule> unavailabilityRules,
            List<Appointment> appointments) {

        // Ordre d'évaluation important pour la priorité des statuts

        // 1. Vérifier si le créneau est dans le passé
        if (slot.isInPast()) {
            return AvailabilityStatus.PAST;
        }

        // 2. Vérifier les indisponibilités (vacances, absences)
        if (isBlockedByAnyUnavailability(slot, unavailabilityRules)) {
            return AvailabilityStatus.UNAVAILABLE;
        }

        // 3. Vérifier les rendez-vous existants
        if (isBlockedByAnyAppointment(slot, appointments)) {
            return AvailabilityStatus.BOOKED;
        }

        // 4. Le créneau est disponible
        return AvailabilityStatus.AVAILABLE;
    }

    // ========== EXTRACTION ET RECHERCHE ==========

    @Override
    public List<TimeSlot> extractAvailableSlots(List<AvailableSlot> markedSlots) {
        if (markedSlots == null || markedSlots.isEmpty()) {
            return Collections.emptyList();
        }

        return markedSlots.stream()
                .filter(slot -> slot.status() == AvailabilityStatus.AVAILABLE)
                .map(AvailableSlot::timeSlot)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlot> filterSlotsByStatus(
            List<AvailableSlot> markedSlots,
            AvailabilityStatus status) {

        if (markedSlots == null || status == null) {
            return Collections.emptyList();
        }

        return markedSlots.stream()
                .filter(slot -> slot.status() == status)
                .map(AvailableSlot::timeSlot)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TimeSlot> findNextAvailableSlot(
            List<AvailableSlot> availableSlots,
            LocalDateTime afterDateTime,
            int requiredDuration) {

        if (availableSlots == null || afterDateTime == null) {
            return Optional.empty();
        }

        return availableSlots.stream()
                .filter(slot -> slot.status() == AvailabilityStatus.AVAILABLE)
                .map(AvailableSlot::timeSlot)
                .filter(slot -> slot.startDateTime().isAfter(afterDateTime))
                .filter(slot -> slot.duration() >= requiredDuration)
                .findFirst();
    }

    @Override
    public List<TimeSlot> findSlotsByMinDuration(
            List<AvailableSlot> availableSlots,
            int minDuration) {

        if (availableSlots == null) {
            return Collections.emptyList();
        }

        return availableSlots.stream()
                .filter(slot -> slot.status() == AvailabilityStatus.AVAILABLE)
                .map(AvailableSlot::timeSlot)
                .filter(slot -> slot.duration() >= minDuration)
                .collect(Collectors.toList());
    }

    // ========== VALIDATION ==========

    @Override
    public boolean isSpecificSlotAvailable(
            TimeSlot requestedSlot,
            List<AvailableSlot> availableSlots) {

        if (requestedSlot == null || availableSlots == null) {
            return false;
        }

        return availableSlots.stream()
                .anyMatch(slot ->
                        slot.timeSlot().equals(requestedSlot) &&
                                slot.status() == AvailabilityStatus.AVAILABLE
                );
    }

    @Override
    public boolean isSlotBookable(TimeSlot slot) {
        if (slot == null) {
            return false;
        }

        // Le créneau ne doit pas être dans le passé
        if (slot.isInPast()) {
            return false;
        }

        // Vérifier la durée minimale
        if (slot.duration() < MIN_SLOT_DURATION) {
            return false;
        }

        // Vérifier la durée maximale
        if (slot.duration() > MAX_SLOT_DURATION) {
            return false;
        }

        // Vérifier que ce n'est pas trop loin dans le futur
        if (slot.startDateTime().isAfter(LocalDateTime.now().plusDays(MAX_BOOKING_ADVANCE_DAYS))) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isAppointmentBlocking(Appointment appointment, TimeSlot slot) {
        if (appointment == null || slot == null) {
            return false;
        }

        // Seuls les rendez-vous actifs bloquent les créneaux
        AppointmentStatus status = appointment.getStatus();
        boolean isActive = status == AppointmentStatus.PENDING ||
                status == AppointmentStatus.CONFIRMED;

        return isActive && appointment.getSlot().overlaps(slot);
    }

    @Override
    public boolean isUnavailabilityBlocking(UnavailabilityRule unavailabilityRule, TimeSlot slot) {
        if (unavailabilityRule == null || slot == null) {
            return false;
        }

        return unavailabilityRule.blocksTimeSlot(slot);
    }

    // ========== UTILITAIRES ==========

    @Override
    public Map<LocalDate, List<TimeSlot>> groupSlotsByDay(List<TimeSlot> slots) {
        if (slots == null || slots.isEmpty()) {
            return Collections.emptyMap();
        }

        return slots.stream()
                .collect(Collectors.groupingBy(
                        slot -> slot.startDateTime().toLocalDate(),
                        TreeMap::new, // Pour avoir les dates triées
                        Collectors.toList()
                ));
    }

    @Override
    public List<TimeSlot> sortSlotsByDateTime(List<TimeSlot> slots) {
        if (slots == null || slots.isEmpty()) {
            return slots;
        }

        return slots.stream()
                .sorted(Comparator.comparing(TimeSlot::startDateTime))
                .collect(Collectors.toList());
    }

    @Override
    public SlotStatistics calculateStatistics(List<AvailableSlot> availableSlots) {
        if (availableSlots == null || availableSlots.isEmpty()) {
            return new SlotStatistics(0, 0, 0, 0, 0, 0.0, 0.0);
        }

        int total = availableSlots.size();

        Map<AvailabilityStatus, Long> statusCounts = availableSlots.stream()
                .collect(Collectors.groupingBy(
                        AvailableSlot::status,
                        Collectors.counting()
                ));

        int available = statusCounts.getOrDefault(AvailabilityStatus.AVAILABLE, 0L).intValue();
        int booked = statusCounts.getOrDefault(AvailabilityStatus.BOOKED, 0L).intValue();
        int unavailable = statusCounts.getOrDefault(AvailabilityStatus.UNAVAILABLE, 0L).intValue();
        int past = statusCounts.getOrDefault(AvailabilityStatus.PAST, 0L).intValue();

        double availabilityRate = total > 0 ? (double) available / total * 100 : 0.0;
        double bookingRate = (available + booked) > 0 ?
                (double) booked / (available + booked) * 100 : 0.0;

        return new SlotStatistics(
                total,
                available,
                booked,
                unavailable,
                past,
                availabilityRate,
                bookingRate
        );
    }

    @Override
    public List<TimeSlot> mergeContiguousSlots(List<TimeSlot> slots) {
        if (slots == null || slots.size() <= 1) {
            return slots;
        }

        // Trier d'abord les créneaux
        List<TimeSlot> sortedSlots = sortSlotsByDateTime(slots);
        List<TimeSlot> merged = new ArrayList<>();

        TimeSlot current = sortedSlots.get(0);

        for (int i = 1; i < sortedSlots.size(); i++) {
            TimeSlot next = sortedSlots.get(i);

            // Vérifier si les créneaux sont contigus
            if (current.getEndDateTime().equals(next.startDateTime())) {
                // Fusionner les créneaux
                current = new TimeSlot(
                        current.startDateTime(),
                        current.duration() + next.duration()
                );
            } else {
                merged.add(current);
                current = next;
            }
        }

        merged.add(current);
        return merged;
    }

    // ========== MÉTHODES PRIVÉES HELPER ==========

    private boolean isBlockedByAnyUnavailability(
            TimeSlot slot,
            List<UnavailabilityRule> unavailabilityRules) {

        if (unavailabilityRules == null || unavailabilityRules.isEmpty()) {
            return false;
        }

        return unavailabilityRules.stream()
                .anyMatch(rule -> isUnavailabilityBlocking(rule, slot));
    }

    private boolean isBlockedByAnyAppointment(
            TimeSlot slot,
            List<Appointment> appointments) {

        if (appointments == null || appointments.isEmpty()) {
            return false;
        }

        return appointments.stream()
                .anyMatch(apt -> isAppointmentBlocking(apt, slot));
    }
}