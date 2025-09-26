package com.presta.domain.service;


import com.presta.domain.model.Appointment;
import com.presta.domain.model.AvailabilityRule;
import com.presta.domain.model.UnavailabilityRule;
import com.presta.domain.model.valueobject.AppointmentStatus;
import com.presta.domain.model.valueobject.TimeSlot;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Service métier - Logique pure du domaine pour les rendez-vous
 * Aucune dépendance externe, totalement testable unitairement
 */
public class AppointmentDomainService {

    /**
     * Vérifie si un nouveau rendez-vous peut être créé
     * @return true si toutes les règles métier sont respectées
     */
    public boolean canCreateAppointment(
            LocalDateTime proposedDateTime,
            int duration,
            List<AvailabilityRule> contractorRules,
            List<UnavailabilityRule> unavailabilities,
            List<Appointment> existingAppointments) {

        // 1. Vérifier qu'il existe au moins une règle de disponibilité
        if (contractorRules == null || contractorRules.isEmpty()) {
            return false;
        }

        // 2. Vérifier que le créneau correspond à une règle de disponibilité
        if (!isWithinAvailabilityRules(proposedDateTime, duration, contractorRules)) {
            return false;
        }

        // 3. Vérifier qu'il n'y a pas d'indisponibilité sur ce créneau
        if (isBlockedByUnavailability(proposedDateTime, duration, unavailabilities)) {
            return false;
        }

        // 4. Vérifier qu'il n'y a pas de conflit avec des RDV existants
        if (hasConflictWithExistingAppointments(proposedDateTime, duration, existingAppointments)) {
            return false;
        }

        return true;
    }

    /**
     * Vérifie si le créneau proposé correspond aux règles de disponibilité
     */
    public boolean isWithinAvailabilityRules(
            LocalDateTime proposedDateTime,
            int duration,
            List<AvailabilityRule> rules) {

        DayOfWeek dayOfWeek = proposedDateTime.getDayOfWeek();
        LocalTime startTime = proposedDateTime.toLocalTime();
        LocalTime endTime = startTime.plusMinutes(duration);

        for (AvailabilityRule rule : rules) {
            // Vérifier si la règle s'applique à ce jour
            if (!rule.appliesTo(dayOfWeek)) {
                continue;
            }

            // Vérifier si le créneau est dans la plage horaire
            if (!rule.isWithinTimeRange(startTime) ||
                    !rule.isWithinTimeRange(endTime.minusMinutes(1))) {
                continue;
            }

            // Vérifier que la durée correspond à la configuration
            if (duration != rule.getSlotConfig().slotDuration()) {
                continue;
            }

            // Vérifier que le créneau respecte la grille horaire
            if (!isAlignedWithSlotGrid(proposedDateTime, rule)) {
                continue;
            }

            return true; // Au moins une règle correspond
        }

        return false; // Aucune règle ne correspond
    }

    /**
     * Vérifie que le créneau est aligné sur la grille horaire
     * Ex: Si les créneaux commencent à 9h avec 30min + 10min repos,
     * les créneaux valides sont 9h00, 9h40, 10h20, etc.
     */
    public boolean isAlignedWithSlotGrid(LocalDateTime proposedDateTime, AvailabilityRule rule) {
        LocalTime ruleStart = rule.getTimeRange().startTime();
        LocalTime proposedTime = proposedDateTime.toLocalTime();

        // Calculer le temps écoulé depuis le début de la règle
        long minutesFromStart = java.time.Duration.between(ruleStart, proposedTime).toMinutes();
        if (minutesFromStart < 0) {
            return false;
        }

        // Vérifier l'alignement sur la grille (slot + repos)
        int totalSlotTime = rule.getSlotConfig().slotDuration() + rule.getSlotConfig().restTime();
        return minutesFromStart % totalSlotTime == 0;
    }

    /**
     * Vérifie si le créneau est bloqué par une indisponibilité
     */
    public boolean isBlockedByUnavailability(
            LocalDateTime proposedDateTime,
            int duration,
            List<UnavailabilityRule> unavailabilities) {

        if (unavailabilities == null || unavailabilities.isEmpty()) {
            return false;
        }

        TimeSlot proposedSlot = new TimeSlot(proposedDateTime, duration);

        for (UnavailabilityRule unavailability : unavailabilities) {
            if (unavailability.blocksTimeSlot(proposedSlot)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Vérifie s'il y a un conflit avec des rendez-vous existants
     */
    public boolean hasConflictWithExistingAppointments(
            LocalDateTime proposedDateTime,
            int duration,
            List<Appointment> existingAppointments) {

        if (existingAppointments == null || existingAppointments.isEmpty()) {
            return false;
        }

        for (Appointment appointment : existingAppointments) {
            // Ignorer les RDV annulés ou terminés
            if (appointment.getStatus() == AppointmentStatus.CANCELLED ||
                    appointment.getStatus() == AppointmentStatus.COMPLETED) {
                continue;
            }

            // Vérifier le chevauchement
            if (appointment.blocksTimeSlot(proposedDateTime, duration)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Calcule le prochain créneau disponible après une date donnée
     */
    public LocalDateTime findNextAvailableSlot(
            LocalDateTime afterDateTime,
            int requiredDuration,
            List<AvailabilityRule> rules,
            List<UnavailabilityRule> unavailabilities,
            List<Appointment> existingAppointments) {

        LocalDateTime searchDate = afterDateTime;
        int maxDaysToSearch = 90; // Limite de recherche

        for (int day = 0; day < maxDaysToSearch; day++) {
            LocalDate currentDate = searchDate.toLocalDate().plusDays(day);

            // Générer tous les créneaux possibles pour cette journée
            List<LocalDateTime> possibleSlots = generatePossibleSlotsForDay(
                    currentDate, requiredDuration, rules
            );

            for (LocalDateTime slot : possibleSlots) {
                if (slot.isAfter(afterDateTime) &&
                        canCreateAppointment(slot, requiredDuration, rules, unavailabilities, existingAppointments)) {
                    return slot;
                }
            }
        }

        return null; // Aucun créneau trouvé dans les 90 prochains jours
    }

    /**
     * Génère tous les créneaux possibles pour une journée selon les règles
     */
    private List<LocalDateTime> generatePossibleSlotsForDay(
            LocalDate date,
            int duration,
            List<AvailabilityRule> rules) {

        List<LocalDateTime> slots = new java.util.ArrayList<>();
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        for (AvailabilityRule rule : rules) {
            if (!rule.appliesTo(dayOfWeek) || !rule.isActive()) {
                continue;
            }

            if (rule.getSlotConfig().slotDuration() != duration) {
                continue;
            }

            // Générer les créneaux selon la grille
            LocalDateTime currentSlot = LocalDateTime.of(date, rule.getTimeRange().startTime());
            LocalDateTime endOfDay = LocalDateTime.of(date, rule.getTimeRange().endTime());
            int totalSlotTime = rule.getSlotConfig().slotDuration() + rule.getSlotConfig().restTime();

            while (currentSlot.plusMinutes(duration).isBefore(endOfDay) ||
                    currentSlot.plusMinutes(duration).equals(endOfDay)) {
                slots.add(currentSlot);
                currentSlot = currentSlot.plusMinutes(totalSlotTime);
            }
        }

        return slots;
    }

    /**
     * Calcule le taux d'occupation d'un prestataire sur une période
     */
    public double calculateOccupancyRate(
            List<Appointment> appointments,
            List<AvailabilityRule> rules,
            LocalDate startDate,
            LocalDate endDate) {

        if (rules.isEmpty() || startDate.isAfter(endDate)) {
            return 0.0;
        }

        // Calculer le nombre total de créneaux possibles
        int totalPossibleSlots = 0;
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            for (AvailabilityRule rule : rules) {
                if (rule.appliesTo(currentDate.getDayOfWeek()) && rule.isActive()) {
                    List<TimeSlot> dailySlots = rule.generateSlotsForDay(currentDate);
                    totalPossibleSlots += dailySlots.size();
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        if (totalPossibleSlots == 0) {
            return 0.0;
        }

        // Compter les créneaux occupés (confirmés ou en attente)
        long occupiedSlots = appointments.stream()
                .filter(apt -> !apt.getSlot().startDateTime().toLocalDate().isBefore(startDate))
                .filter(apt -> !apt.getSlot().startDateTime().toLocalDate().isAfter(endDate))
                .filter(apt -> apt.getStatus() == AppointmentStatus.CONFIRMED ||
                        apt.getStatus() == AppointmentStatus.PENDING)
                .count();

        return (double) occupiedSlots / totalPossibleSlots * 100;
    }

    /**
     * Vérifie si un client peut annuler un rendez-vous
     * Règles : propriétaire du RDV, plus de 24h avant, statut modifiable
     */
    public boolean canClientCancelAppointment(Appointment appointment, UUID clientId) {
        return appointment.getClientId().equals(clientId) &&
                appointment.getSlot().isInMoreThan24h() &&
                appointment.getStatus().canBeCancelled();
    }

    /**
     * Vérifie si un prestataire peut annuler un rendez-vous
     * Règles : propriétaire du RDV, statut modifiable (pas de limite 24h pour le prestataire)
     */
    public boolean canContractorCancelAppointment(Appointment appointment, UUID contractorId) {
        return appointment.getContractorId().equals(contractorId) &&
                appointment.getStatus().canBeCancelled();
    }

    /**
     * Détermine si un rappel doit être envoyé pour un rendez-vous
     * Règle : RDV confirmé dans les prochaines 24-48h
     */
    public boolean shouldSendReminder(Appointment appointment, LocalDateTime now) {
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            return false;
        }

        long hoursUntilAppointment = java.time.Duration
                .between(now, appointment.getSlot().startDateTime())
                .toHours();

        return hoursUntilAppointment >= 24 && hoursUntilAppointment <= 48;
    }
}