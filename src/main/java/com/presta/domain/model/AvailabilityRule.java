package com.presta.domain.model;

import com.presta.domain.model.valueobject.SlotConfiguration;
import com.presta.domain.model.valueobject.TimeRange;
import com.presta.domain.model.valueobject.TimeSlot;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class AvailabilityRule {
    private final UUID id;
    private final UUID contractorId;
    private final boolean isContractorActive;
    private Set<DayOfWeek> weekDays;
    private TimeRange timeRange;
    private SlotConfiguration slotConfig;
    private List<BreakTime> breakTimes;
    private boolean isActive; // contractor

    // Factory method pour création
    public static AvailabilityRule create(
            UUID contractorId,
            boolean isContractorActive,
            Set<DayOfWeek> weekDays,
            TimeRange timeRange,
            SlotConfiguration slotConfig) {
        return new AvailabilityRule(
                UUID.randomUUID(),
                contractorId,
                isContractorActive,
                weekDays,
                timeRange,
                slotConfig,
                List.of(),  // Pas de pauses par défaut
                true
        );
    }

    // Constructor complet pour reconstitution
    public AvailabilityRule(
            UUID id,
            UUID contractorId, boolean isContractorActive,
            Set<DayOfWeek> weekDays,
            TimeRange timeRange,
            SlotConfiguration slotConfig,
            List<BreakTime> breakTimes,
            boolean isActive) {


        validateBusinessRules(contractorId, weekDays, timeRange, slotConfig);

        this.id = id;
        this.contractorId = contractorId;
        this.weekDays = Set.copyOf(weekDays); // Copie défensive
        this.timeRange = timeRange;
        this.slotConfig = slotConfig;
        this.breakTimes = breakTimes != null ? new ArrayList<>(breakTimes) : new ArrayList<>();
        this.isActive = isActive;
        this.isContractorActive = isContractorActive;
    }

    // Business Methods
    public void addBreakTime(BreakTime breakTime) {
        if (breakTime == null) {
            throw new IllegalArgumentException("Le temps de pause ne peut être null");
        }

        // Vérifier que la pause est dans la plage horaire de travail
        if (breakTime.timeRange().startTime().isBefore(timeRange.startTime()) ||
                breakTime.timeRange().endTime().isAfter(timeRange.endTime())) {
            throw new IllegalArgumentException(
                    "La pause doit être dans la plage horaire de travail"
            );
        }

        // Vérifier qu'elle ne chevauche pas avec une pause existante
        for (BreakTime existing : breakTimes) {
            if (existing.timeRange().overlaps(breakTime.timeRange())) {
                throw new IllegalArgumentException(
                        "Cette pause chevauche avec une pause existante"
                );
            }
        }

        this.breakTimes.add(breakTime);
    }

    public void removeBreakTime(BreakTime breakTime) {
        this.breakTimes.remove(breakTime);
    }

    public void clearBreakTimes() {
        this.breakTimes.clear();
    }
    public void updateSchedule(Set<DayOfWeek> newWeekDays, TimeRange newTimeRange) {
        if (newWeekDays == null || newWeekDays.isEmpty()) {
            throw new IllegalArgumentException("Au moins un jour doit être sélectionné");
        }
        this.weekDays = Set.copyOf(newWeekDays);
        this.timeRange = newTimeRange;
    }

    public void updateSlotConfiguration(SlotConfiguration newConfig) {
        if (newConfig == null) {
            throw new IllegalArgumentException("La configuration est obligatoire");
        }
        this.slotConfig = newConfig;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    // Query Methods
    public boolean appliesTo(DayOfWeek dayOfWeek) {
        return isActive && weekDays.contains(dayOfWeek);
    }

    public boolean isWithinTimeRange(LocalTime time) {
        return timeRange.contains(time);
    }

    /**
     * Génère tous les créneaux pour une date donnée
     */
    public List<TimeSlot> generateSlotsForDate(LocalDate date) {
        if (!appliesTo(date.getDayOfWeek())) {
            return List.of();
        }

        List<TimeSlot> slots = new ArrayList<>();
        LocalDateTime currentStart = LocalDateTime.of(date, timeRange.startTime());
        LocalDateTime dayEnd = LocalDateTime.of(date, timeRange.endTime());

        while (currentStart.plusMinutes(slotConfig.slotDuration()).isBefore(dayEnd) ||
                currentStart.plusMinutes(slotConfig.slotDuration()).equals(dayEnd)) {

            LocalDateTime slotEnd = currentStart.plusMinutes(slotConfig.slotDuration());

            boolean isDuringBreak = false;
            for (BreakTime breakTime : breakTimes) {
                if (breakTime.overlapsWithSlot(currentStart, slotEnd)) {
                    isDuringBreak = true;
                    break;
                }
            }

            if (!isDuringBreak) {
                slots.add(new TimeSlot(
                        currentStart,
                        slotConfig.slotDuration()
                ));
            }

            currentStart = slotEnd.plusMinutes(slotConfig.restTime());
        }

        return slots;
    }

    // Validation
    private void validateBusinessRules(
            UUID contractorId,
            Set<DayOfWeek> weekDays,
            TimeRange timeRange,
            SlotConfiguration slotConfig) {

        if (contractorId == null) {
            throw new IllegalArgumentException("L'ID du prestataire est obligatoire");
        }

        if (weekDays == null || weekDays.isEmpty()) {
            throw new IllegalArgumentException("Au moins un jour doit être sélectionné");
        }

        if (timeRange == null) {
            throw new IllegalArgumentException("La plage horaire est obligatoire");
        }

        if (slotConfig == null) {
            throw new IllegalArgumentException("La configuration est obligatoire");
        }

        if(!isContractorActive){
            throw new IllegalArgumentException("Le prestataire doit etre actif");
        }

        // Vérifier que la plage permet au moins un créneau
        if (timeRange.getDurationInMinutes() < slotConfig.slotDuration()) {
            throw new IllegalArgumentException(
                    "La plage horaire doit permettre au moins un créneau"
            );
        }
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getContractorId() { return contractorId; }
    public Set<DayOfWeek> getWeekDays() { return Set.copyOf(weekDays); }
    public TimeRange getTimeRange() { return timeRange; }
    public SlotConfiguration getSlotConfig() { return slotConfig; }
    public List<BreakTime> getBreakTimes() { return List.copyOf(breakTimes); }
    public boolean isActive() { return isActive; }
}

//public class AvailabilityRule {
//    private final UUID id;
//    private final UUID contractorId;
//    private Set<DayOfWeek> weekDays;
//    private TimeRange timeRange;
//    private SlotConfiguration slotConfig;
//    private boolean isActive;
//
//    public static AvailabilityRule create(
//            UUID contractorId,
//            Set<DayOfWeek> weekDays,
//            TimeRange timeRange,
//            SlotConfiguration slotConfig) {
//        return new AvailabilityRule(
//                UUID.randomUUID(), // je suis pas sur de ça puttttttain
//                contractorId,
//                weekDays,
//                timeRange,
//                slotConfig,
//                true
//        );
//    }
//
//    public AvailabilityRule(
//            UUID id,
//            UUID contractorId,
//            Set<DayOfWeek> weekDays,
//            TimeRange timeRange,
//            SlotConfiguration slotConfig,
//            boolean isActive) {
//
//        validateBusinessRules(contractorId, weekDays, timeRange, slotConfig);
//
//        this.id = id;
//        this.contractorId = contractorId;
//        this.weekDays = Set.copyOf(weekDays);
//        this.timeRange = timeRange;
//        this.slotConfig = slotConfig;
//        this.isActive = isActive;
//    }
//
//    // Logique métier
//    public void updateSchedule(Set<DayOfWeek> newWeekDays, TimeRange newTimeRange) {
//        if (newWeekDays == null || newWeekDays.isEmpty()) {
//            throw new IllegalArgumentException("Au moins un jour doit être sélectionné");
//        }
//        this.weekDays = Set.copyOf(newWeekDays);
//        this.timeRange = newTimeRange;
//    }
//
//    public void updateSlotConfiguration(SlotConfiguration newConfig) {
//        if (newConfig == null) {
//            throw new IllegalArgumentException("La configuration est obligatoire");
//        }
//        this.slotConfig = newConfig;
//    }
//
//    public void activate() {
//        this.isActive = true;
//    }
//
//    public void deactivate() {
//        this.isActive = false;
//    }
//
//    // Query Methods
//    public boolean appliesTo(DayOfWeek dayOfWeek) {
//        return isActive && weekDays.contains(dayOfWeek);
//    }
//
//    public boolean isWithinTimeRange(LocalTime time) {
//        return timeRange.contains(time);
//    }
//
//    /**
//     * Génère tous les créneaux pour une date donnée
//     */
//    public List<TimeSlot> generateSlotsForDate(LocalDate date) {
//        if (!appliesTo(date.getDayOfWeek())) {
//            return List.of();
//        }
//
//        List<TimeSlot> slots = new ArrayList<>();
//        LocalDateTime currentStart = LocalDateTime.of(date, timeRange.startTime());
//        LocalDateTime dayEnd = LocalDateTime.of(date, timeRange.endTime());
//
//        while (currentStart.plusMinutes(slotConfig.slotDuration()).isBefore(dayEnd) ||
//                currentStart.plusMinutes(slotConfig.slotDuration()).equals(dayEnd)) {
//
//            LocalDateTime slotEnd = currentStart.plusMinutes(slotConfig.slotDuration());
//
//            slots.add(new TimeSlot(
//                    contractorId,
//                    currentStart,
//                    slotEnd,
//                    slotConfig.slotDuration(),
//                    true // Par défaut disponible, sera filtré après
//            ));
//
//            // Avancer au prochain créneau avec le temps de repos
//            currentStart = slotEnd.plusMinutes(slotConfig.restTime());
//        }
//
//        return slots;
//    }
//
//    // Validation
//    private void validateBusinessRules(
//            UUID contractorId,
//            Set<DayOfWeek> weekDays,
//            TimeRange timeRange,
//            SlotConfiguration slotConfig) {
//
//        if (contractorId == null) {
//            throw new IllegalArgumentException("L'ID du prestataire est obligatoire");
//        }
//
//        if (weekDays == null || weekDays.isEmpty()) {
//            throw new IllegalArgumentException("Au moins un jour doit être sélectionné");
//        }
//
//        if (timeRange == null) {
//            throw new IllegalArgumentException("La plage horaire est obligatoire");
//        }
//
//        if (slotConfig == null) {
//            throw new IllegalArgumentException("La configuration est obligatoire");
//        }
//
//        // Vérifier que la plage permet au moins un créneau
//        if (timeRange.getDurationInMinutes() < slotConfig.slotDuration()) {
//            throw new IllegalArgumentException(
//                    "La plage horaire doit permettre au moins un créneau"
//            );
//        }
//    }
//
//    // Getters
//    public UUID getId() { return id; }
//    public UUID getContractorId() { return contractorId; }
//    public Set<DayOfWeek> getWeekDays() { return Set.copyOf(weekDays); }
//    public TimeRange getTimeRange() { return timeRange; }
//    public SlotConfiguration getSlotConfig() { return slotConfig; }
//    public boolean isActive() { return isActive; }
//}