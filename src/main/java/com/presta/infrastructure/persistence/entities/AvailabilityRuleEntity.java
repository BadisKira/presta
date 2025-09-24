package com.presta.infrastructure.persistence.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Entity JPA pour availability_rule
 * Correspond exactement à la table PostgreSQL
 */
@Entity
@Table(name = "availability_rule", schema = "presta")
public class AvailabilityRuleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "contractor_id", nullable = false)
    private UUID contractorId;

    @Column(name = "week_days", nullable = false, columnDefinition = "integer[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private Integer[] weekDays; // Array PostgreSQL [1,2,3,4,5,6,7]

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "slot_duration", nullable = false)
    private Integer slotDuration; // en minutes

    @Column(name = "rest_time")
    private Integer restTime = 0; // temps entre RDV en minutes

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(
            mappedBy = "availabilityRule",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<BreakTimeEntity> breakTimes = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructeurs
    public AvailabilityRuleEntity() {
    }

    public AvailabilityRuleEntity(UUID contractorId, Integer[] weekDays,
                                  LocalTime startTime, LocalTime endTime,
                                  Integer slotDuration, Integer restTime,
                                  Boolean isActive) {
        this.contractorId = contractorId;
        this.weekDays = weekDays;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotDuration = slotDuration;
        this.restTime = restTime;
        this.isActive = isActive;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters et Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getContractorId() {
        return contractorId;
    }

    public void setContractorId(UUID contractorId) {
        this.contractorId = contractorId;
    }

    public Integer[] getWeekDays() {
        return weekDays;
    }

    public void setWeekDays(Integer[] weekDays) {
        this.weekDays = weekDays;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getSlotDuration() {
        return slotDuration;
    }

    public void setSlotDuration(Integer slotDuration) {
        this.slotDuration = slotDuration;
    }

    public Integer getRestTime() {
        return restTime;
    }

    public void setRestTime(Integer restTime) {
        this.restTime = restTime;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public List<BreakTimeEntity> getBreakTimes() {
        return breakTimes;
    }

    public void setBreakTimes(List<BreakTimeEntity> breakTimes) {
        this.breakTimes = breakTimes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Méthodes helper pour gérer les BreakTimes
    public void addBreakTime(BreakTimeEntity breakTime) {
        breakTimes.add(breakTime);
        breakTime.setAvailabilityRule(this);
    }

    public void removeBreakTime(BreakTimeEntity breakTime) {
        breakTimes.remove(breakTime);
        breakTime.setAvailabilityRule(null);
    }

    public void clearBreakTimes() {
        for (BreakTimeEntity breakTime : new ArrayList<>(breakTimes)) {
            removeBreakTime(breakTime);
        }
    }

    @Override
    public String toString() {
        return "AvailabilityRuleEntity{" +
                "id=" + id +
                ", contractorId=" + contractorId +
                ", weekDays=" + Arrays.toString(weekDays) +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", slotDuration=" + slotDuration +
                ", restTime=" + restTime +
                ", isActive=" + isActive +
                ", breakTimes=" + breakTimes +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}