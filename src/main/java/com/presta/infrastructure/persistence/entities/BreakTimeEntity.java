package com.presta.infrastructure.persistence.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Entity JPA pour break_time
 * Correspond exactement à la table PostgreSQL
 */
@Entity
@Table(name = "break_time", schema = "presta")
public class BreakTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "availability_rule_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_availability_rule"))
    private AvailabilityRuleEntity availabilityRule;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "week_days", columnDefinition = "integer[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private Integer[] weekDays; // null = tous les jours de la règle

    // Constructeurs
    public BreakTimeEntity() {
    }

    public BreakTimeEntity(LocalTime startTime, LocalTime endTime, Integer[] weekDays) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.weekDays = weekDays;
    }

    // Getters et Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AvailabilityRuleEntity getAvailabilityRule() {
        return availabilityRule;
    }

    public void setAvailabilityRule(AvailabilityRuleEntity availabilityRule) {
        this.availabilityRule = availabilityRule;
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

    public Integer[] getWeekDays() {
        return weekDays;
    }

    public void setWeekDays(Integer[] weekDays) {
        this.weekDays = weekDays;
    }
}