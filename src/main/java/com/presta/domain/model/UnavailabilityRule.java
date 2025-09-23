package com.presta.domain.model;

import com.presta.domain.model.valueobject.TimeSlot;
import com.presta.domain.model.valueobject.UnavailabilityPeriod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public class UnavailabilityRule {
    private final UUID id;
    private final UUID contractorId;
    private final UnavailabilityPeriod period;
    private final String reason;
    private final LocalDateTime createdAt;

    // Factory method pour création
    public static UnavailabilityRule create(
            UUID contractorId,
            LocalDate startDate,
            LocalDate endDate,
            LocalTime startTime,
            LocalTime endTime,
            String reason) {

        return new UnavailabilityRule(
                UUID.randomUUID(),
                contractorId,
                new UnavailabilityPeriod(startDate, endDate, startTime, endTime),
                reason,
                LocalDateTime.now()
        );
    }

    // Factory method pour indisponibilité journée complète
    public static UnavailabilityRule createFullDay(
            UUID contractorId,
            LocalDate startDate,
            LocalDate endDate,
            String reason) {

        return create(contractorId, startDate, endDate, null, null, reason);
    }

    // Factory method pour indisponibilité d'une seule journée
    public static UnavailabilityRule createSingleDay(
            UUID contractorId,
            LocalDate date,
            String reason) {

        return createFullDay(contractorId, date, date, reason);
    }

    // Constructor complet pour reconstitution
    public UnavailabilityRule(
            UUID id,
            UUID contractorId,
            UnavailabilityPeriod period,
            String reason,
            LocalDateTime createdAt) {

        if (id == null || contractorId == null || period == null) {
            throw new IllegalArgumentException("Les champs obligatoires ne peuvent être null");
        }

        this.id = id;
        this.contractorId = contractorId;
        this.period = period;
        this.reason = reason;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    // Business Methods

    /**
     * Vérifie si cette règle est active pour une date donnée
     */
    public boolean isActiveOn(LocalDate date) {
        return !date.isBefore(period.startDate()) && !date.isAfter(period.endDate());
    }

    /**
     * Vérifie si cette règle bloque un créneau spécifique
     */
    public boolean blocksTimeSlot(TimeSlot slot) {
        return period.overlapsWithSlot(slot.startDateTime(), slot.getEndDateTime());
    }

    /**
     * Vérifie si cette règle bloque un moment précis
     */
    public boolean blocksDateTime(LocalDateTime dateTime) {
        return period.contains(dateTime);
    }

    /**
     * Vérifie si cette règle est dans le futur
     */
    public boolean isFuture() {
        return period.startDate().isAfter(LocalDate.now());
    }

    /**
     * Vérifie si cette règle est passée
     */
    public boolean isPast() {
        return period.endDate().isBefore(LocalDate.now());
    }

    /**
     * Vérifie si cette règle est actuellement active
     */
    public boolean isCurrentlyActive() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(period.startDate()) && !today.isAfter(period.endDate());
    }

    /**
     * Prolonge la période d'indisponibilité
     */
    public UnavailabilityRule extendPeriod(LocalDate newEndDate) {
        if (newEndDate.isBefore(period.endDate())) {
            throw new IllegalArgumentException(
                    "La nouvelle date de fin doit être après l'actuelle"
            );
        }

        return new UnavailabilityRule(
                this.id,
                this.contractorId,
                new UnavailabilityPeriod(
                        period.startDate(),
                        newEndDate,
                        period.startTime(),
                        period.endTime()
                ),
                this.reason,
                this.createdAt
        );
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getContractorId() { return contractorId; }
    public UnavailabilityPeriod getPeriod() { return period; }
    public LocalDate getStartDate() { return period.startDate(); }
    public LocalDate getEndDate() { return period.endDate(); }
    public LocalTime getStartTime() { return period.startTime(); }
    public LocalTime getEndTime() { return period.endTime(); }
    public String getReason() { return reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isFullDay() { return period.isFullDay(); }
}
