package com.presta.domain.service;

import com.presta.domain.exception.AppointmentConflictException;
import com.presta.domain.exception.NoAvailabilityRulesException;
import com.presta.domain.exception.OutsideAvailabilityException;
import com.presta.domain.exception.UnavailabilityConflictException;
import com.presta.domain.model.Appointment;
import com.presta.domain.model.AvailabilityRule;
import com.presta.domain.model.UnavailabilityRule;
import com.presta.domain.model.valueobject.AppointmentStatus;
import com.presta.domain.model.valueobject.TimeSlot;

import java.time.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AppointmentDomainService {

    private static final int MAX_SEARCH_DAYS = 90;
    private static final int REMINDER_MIN_HOURS = 24;
    private static final int REMINDER_MAX_HOURS = 48;

    public boolean canCreateAppointment(
            LocalDateTime proposedDateTime,
            int duration,
            List<AvailabilityRule> contractorRules,
            List<UnavailabilityRule> unavailabilities,
            List<Appointment> existingAppointments) {

        if (!hasAvailabilityRules(contractorRules)) {
            throw new NoAvailabilityRulesException(
                    "Aucune règle de disponibilité n'est définie pour ce prestataire",
                    "NOT FOUND",
                     404

            );
        }

        if (!isWithinAvailabilityRules(proposedDateTime, duration, contractorRules)) {
            throw new OutsideAvailabilityException(
                    String.format("Le créneau proposé (%s, durée: %d min) ne correspond à aucune règle de disponibilité",
                            proposedDateTime, duration),
                    "ERROR",
                    400
            );
        }

        if (isBlockedByUnavailability(proposedDateTime, duration, unavailabilities)) {
            throw new UnavailabilityConflictException(
                    String.format("Le créneau proposé (%s) est bloqué par une période d'indisponibilité",
                            proposedDateTime)
                    ,"ERROR",
                    409
            );
        }

        if (hasConflictWithExistingAppointments(proposedDateTime, duration, existingAppointments)) {
            throw new AppointmentConflictException(
                    String.format("Le créneau proposé (%s, durée: %d min) entre en conflit avec un rendez-vous existant",
                            proposedDateTime, duration)
                    ,"CONFLICT",
                    409
            );
        }

        return true;
    }

    public boolean isWithinAvailabilityRules(
            LocalDateTime proposedDateTime,
            int duration,
            List<AvailabilityRule> rules) {

        if (rules == null) return false;

        return rules.stream()
                .filter(rule -> rule.appliesTo(proposedDateTime.getDayOfWeek()))
                .anyMatch(rule -> isValidForRule(proposedDateTime, duration, rule));
    }

    private boolean isValidForRule(LocalDateTime proposedDateTime, int duration, AvailabilityRule rule) {
        LocalTime startTime = proposedDateTime.toLocalTime();
        LocalTime endTime = startTime.plusMinutes(duration);

        return rule.isWithinTimeRange(startTime) &&
                rule.isWithinTimeRange(endTime.minusMinutes(1)) &&
                duration == rule.getSlotConfig().slotDuration() &&
                isAlignedWithSlotGrid(proposedDateTime, rule);
    }

    public boolean isAlignedWithSlotGrid(LocalDateTime proposedDateTime, AvailabilityRule rule) {
        long minutesFromStart = Duration.between(
                rule.getTimeRange().startTime(),
                proposedDateTime.toLocalTime()
        ).toMinutes();

        if (minutesFromStart < 0) return false;

        int totalSlotTime = rule.getSlotConfig().slotDuration() + rule.getSlotConfig().restTime();
        return minutesFromStart % totalSlotTime == 0;
    }

    public boolean isBlockedByUnavailability(
            LocalDateTime proposedDateTime,
            int duration,
            List<UnavailabilityRule> unavailabilities) {

        if (unavailabilities == null) return false;

        TimeSlot proposedSlot = new TimeSlot(proposedDateTime, duration);
        return unavailabilities.stream().anyMatch(u -> u.blocksTimeSlot(proposedSlot));
    }

    public boolean hasConflictWithExistingAppointments(
            LocalDateTime proposedDateTime,
            int duration,
            List<Appointment> existingAppointments) {

        if (existingAppointments == null) return false;

        return existingAppointments.stream()
                .filter(this::isActiveAppointment)
                .anyMatch(apt -> apt.blocksTimeSlot(proposedDateTime, duration));
    }


    public LocalDateTime findNextAvailableSlot(
            LocalDateTime afterDateTime,
            int requiredDuration,
            List<AvailabilityRule> rules,
            List<UnavailabilityRule> unavailabilities,
            List<Appointment> existingAppointments) {

        if (rules == null || rules.isEmpty()) return null;

        var searchDate = afterDateTime.toLocalDate();
        var endDate = searchDate.plusDays(MAX_SEARCH_DAYS);

        while (!searchDate.isAfter(endDate)) {
            for (var rule : rules) {
                if (!rule.appliesTo(searchDate.getDayOfWeek()) || !rule.isActive() ||
                        rule.getSlotConfig().slotDuration() != requiredDuration) continue;

                var totalSlot = rule.getSlotConfig().slotDuration() + rule.getSlotConfig().restTime();
                var ruleStart = rule.getTimeRange().startTime();
                var ruleEnd = rule.getTimeRange().endTime();

                // Calculer le premier créneau valide
                var startTime = searchDate.equals(afterDateTime.toLocalDate()) ?
                        afterDateTime.toLocalTime().isAfter(ruleStart) ? afterDateTime.toLocalTime() : ruleStart
                        : ruleStart;

                // Aligner sur la grille
                var offset = Duration.between(ruleStart, startTime).toMinutes();
                if (offset % totalSlot != 0) {
                    startTime = ruleStart.plusMinutes(((offset / totalSlot) + 1) * totalSlot);
                }

                // Tester chaque créneau de cette règle
                for (var slot = searchDate.atTime(startTime);
                     !slot.toLocalTime().plusMinutes(requiredDuration).isAfter(ruleEnd);
                     slot = slot.plusMinutes(totalSlot)) {

                    // Vérifier uniquement les blocages (pas besoin de re-vérifier les règles)
                    var proposedSlot = new TimeSlot(slot, requiredDuration);

                    boolean blocked = unavailabilities != null &&
                            unavailabilities.stream().anyMatch(u -> u.blocksTimeSlot(proposedSlot));

                    LocalDateTime finalSlot = slot;
                    boolean hasConflict = existingAppointments != null &&
                            existingAppointments.stream()
                                    .filter(apt -> apt.getStatus() != AppointmentStatus.CANCELLED &&
                                            apt.getStatus() != AppointmentStatus.COMPLETED)
                                    .anyMatch(apt -> apt.blocksTimeSlot(finalSlot, requiredDuration));

                    if (!blocked && !hasConflict) {
                        return slot;
                    }
                }
            }
            searchDate = searchDate.plusDays(1);
        }
        return null;
    }

    private List<LocalDateTime> generatePossibleSlotsForDay(
            LocalDate date,
            int duration,
            List<AvailabilityRule> rules) {

        return rules.stream()
                .filter(rule -> isRuleApplicable(rule, date, duration))
                .flatMap(rule -> generateSlotsForRule(date, duration, rule).stream())
                .collect(Collectors.toList());
    }

    private boolean isRuleApplicable(AvailabilityRule rule, LocalDate date, int duration) {
        return rule.appliesTo(date.getDayOfWeek()) &&
                rule.isActive() &&
                rule.getSlotConfig().slotDuration() == duration;
    }

    private List<LocalDateTime> generateSlotsForRule(LocalDate date, int duration, AvailabilityRule rule) {
        List<LocalDateTime> slots = new java.util.ArrayList<>();
        LocalDateTime currentSlot = LocalDateTime.of(date, rule.getTimeRange().startTime());
        LocalDateTime endOfDay = LocalDateTime.of(date, rule.getTimeRange().endTime());
        int totalSlotTime = rule.getSlotConfig().slotDuration() + rule.getSlotConfig().restTime();

        while (!currentSlot.plusMinutes(duration).isAfter(endOfDay)) {
            slots.add(currentSlot);
            currentSlot = currentSlot.plusMinutes(totalSlotTime);
        }

        return slots;
    }

    public double calculateOccupancyRate(
            List<Appointment> appointments,
            List<AvailabilityRule> rules,
            LocalDate startDate,
            LocalDate endDate) {

        if (!isValidPeriod(rules, startDate, endDate)) return 0.0;

        long totalSlots = countTotalPossibleSlots(rules, startDate, endDate);
        if (totalSlots == 0) return 0.0;

        long occupiedSlots = countOccupiedSlots(appointments, startDate, endDate);
        return (double) occupiedSlots / totalSlots * 100;
    }

    private long countTotalPossibleSlots(List<AvailabilityRule> rules, LocalDate startDate, LocalDate endDate) {
        return Stream.iterate(startDate, date -> date.plusDays(1))
                .takeWhile(date -> !date.isAfter(endDate))
                .flatMap(date -> rules.stream()
                        .filter(rule -> rule.appliesTo(date.getDayOfWeek()) && rule.isActive())
                        .flatMap(rule -> rule.generateSlotsForDay(date).stream()))
                .count();
    }

    private long countOccupiedSlots(List<Appointment> appointments, LocalDate startDate, LocalDate endDate) {
        return appointments.stream()
                .filter(apt -> isAppointmentInPeriod(apt, startDate, endDate))
                .filter(this::isActiveAppointment)
                .count();
    }

    public boolean canClientCancelAppointment(Appointment appointment, UUID clientId) {
        return appointment.getClientId().equals(clientId) &&
                appointment.getSlot().isInMoreThan24h() &&
                appointment.getStatus().canBeCancelled();
    }

    public boolean canContractorCancelAppointment(Appointment appointment, UUID contractorId) {
        return appointment.getContractorId().equals(contractorId) &&
                appointment.getStatus().canBeCancelled();
    }

    public boolean shouldSendReminder(Appointment appointment, LocalDateTime now) {
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) return false;

        long hoursUntil = Duration.between(now, appointment.getSlot().startDateTime()).toHours();
        return hoursUntil >= REMINDER_MIN_HOURS && hoursUntil <= REMINDER_MAX_HOURS;
    }

    // Méthodes utilitaires privées
    private boolean hasAvailabilityRules(List<AvailabilityRule> rules) {
        return rules != null && !rules.isEmpty();
    }

    private boolean isActiveAppointment(Appointment appointment) {
        AppointmentStatus status = appointment.getStatus();
        return status != AppointmentStatus.CANCELLED && status != AppointmentStatus.COMPLETED;
    }

    private boolean isAppointmentInPeriod(Appointment apt, LocalDate startDate, LocalDate endDate) {
        LocalDate aptDate = apt.getSlot().startDateTime().toLocalDate();
        return !aptDate.isBefore(startDate) && !aptDate.isAfter(endDate);
    }

    private boolean isValidPeriod(List<AvailabilityRule> rules, LocalDate startDate, LocalDate endDate) {
        return rules != null && !rules.isEmpty() && !startDate.isAfter(endDate);
    }
}