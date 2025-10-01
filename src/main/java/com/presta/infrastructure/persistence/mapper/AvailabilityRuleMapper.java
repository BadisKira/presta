package com.presta.infrastructure.persistence.mapper;

import com.presta.domain.model.AvailabilityRule;
import com.presta.domain.model.BreakTime;
import com.presta.domain.model.valueobject.SlotConfiguration;
import com.presta.domain.model.valueobject.TimeRange;
import com.presta.infrastructure.persistence.entities.AvailabilityRuleEntity;
import com.presta.infrastructure.persistence.entities.BreakTimeEntity;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mapper bidirectionnel entre AvailabilityRule (domaine) et AvailabilityRuleEntity (JPA)
 */
@Component
public class AvailabilityRuleMapper {

    /**
     * Entity → Domain
     * Convertit une entité JPA vers le modèle du domaine
     */
    public AvailabilityRule toDomain(AvailabilityRuleEntity entity) {
        if (entity == null) {
            return null;
        }

        // Convertir Integer[] vers Set<DayOfWeek>
        Set<DayOfWeek> weekDays = convertArrayToDayOfWeekSet(entity.getWeekDays());

        // Reconstituer les Value Objects
        TimeRange timeRange = new TimeRange(
                entity.getStartTime(),
                entity.getEndTime()
        );

        SlotConfiguration slotConfig = new SlotConfiguration(
                entity.getSlotDuration(),
                entity.getRestTime() != null ? entity.getRestTime() : 0
        );

        // Convertir les BreakTimes
        List<BreakTime> breakTimes = new ArrayList<>();
        if (entity.getBreakTimes() != null) {
            for (BreakTimeEntity breakTimeEntity : entity.getBreakTimes()) {
                breakTimes.add(toBreakTimeDomain(breakTimeEntity));
            }
        }

        // Reconstituer l'agrégat AvailabilityRule
        return new AvailabilityRule(
                entity.getId(),
                entity.getContractorId(),
                weekDays,
                timeRange,
                slotConfig,
                breakTimes,
                entity.getIsActive() != null ? entity.getIsActive() : true
        );
    }

    /**
     * Domain → Entity
     * Convertit le modèle du domaine vers une entité JPA
     */
    public AvailabilityRuleEntity toEntity(AvailabilityRule domain) {
        if (domain == null) {
            return null;
        }

        // Créer l'entité
        AvailabilityRuleEntity entity = new AvailabilityRuleEntity();
        entity.setId(domain.getId());
        entity.setContractorId(domain.getContractorId());

        // Convertir Set<DayOfWeek> vers Integer[]
        entity.setWeekDays(convertDayOfWeekSetToArray(domain.getWeekDays()));

        // Décomposer les Value Objects
        entity.setStartTime(domain.getTimeRange().startTime());
        entity.setEndTime(domain.getTimeRange().endTime());
        entity.setSlotDuration(domain.getSlotConfig().slotDuration());
        entity.setRestTime(domain.getSlotConfig().restTime());
        entity.setIsActive(domain.isActive());

        // Timestamps (seront gérés par la DB ou JPA)
        entity.setCreatedAt(java.time.LocalDateTime.now());
        entity.setUpdatedAt(java.time.LocalDateTime.now());

        // Convertir les BreakTimes
        if (domain.getBreakTimes() != null) {
            for (BreakTime breakTime : domain.getBreakTimes()) {
                BreakTimeEntity breakTimeEntity = toBreakTimeEntity(breakTime);
                entity.addBreakTime(breakTimeEntity);
            }
        }

        return entity;
    }

    /**
     * Met à jour une entité existante avec les données du domaine
     */
    public void updateEntity(AvailabilityRuleEntity entity, AvailabilityRule domain) {
        if (entity == null || domain == null) {
            return;
        }

        // Mettre à jour les champs simples
        entity.setWeekDays(convertDayOfWeekSetToArray(domain.getWeekDays()));
        entity.setStartTime(domain.getTimeRange().startTime());
        entity.setEndTime(domain.getTimeRange().endTime());
        entity.setSlotDuration(domain.getSlotConfig().slotDuration());
        entity.setRestTime(domain.getSlotConfig().restTime());
        entity.setIsActive(domain.isActive());
        entity.setUpdatedAt(java.time.LocalDateTime.now());

        // Synchroniser les BreakTimes
        entity.clearBreakTimes();
        if (domain.getBreakTimes() != null) {
            for (BreakTime breakTime : domain.getBreakTimes()) {
                BreakTimeEntity breakTimeEntity = toBreakTimeEntity(breakTime);
                entity.addBreakTime(breakTimeEntity);
            }
        }
    }

    // ========== Méthodes privées pour BreakTime ==========

    /**
     * Convertit BreakTimeEntity vers BreakTime (domain)
     */
    private BreakTime toBreakTimeDomain(BreakTimeEntity entity) {
        if (entity == null) {
            return null;
        }

        TimeRange timeRange = new TimeRange(
                entity.getStartTime(),
                entity.getEndTime()
        );

        Set<DayOfWeek> weekDays = entity.getWeekDays() != null
                ? convertArrayToDayOfWeekSet(entity.getWeekDays())
                : null;

        return new BreakTime(timeRange, weekDays);
    }

    /**
     * Convertit BreakTime (domain) vers BreakTimeEntity
     */
    private BreakTimeEntity toBreakTimeEntity(BreakTime domain) {
        if (domain == null) {
            return null;
        }

        BreakTimeEntity entity = new BreakTimeEntity();
        entity.setStartTime(domain.timeRange().startTime());
        entity.setEndTime(domain.timeRange().endTime());
        entity.setWeekDays(
                domain.weekDays() != null
                        ? convertDayOfWeekSetToArray(domain.weekDays())
                        : null
        );

        return entity;
    }

    // ========== Méthodes utilitaires de conversion ==========

    /**
     * Convertit Integer[] (1-7) vers Set<DayOfWeek>
     */
    private Set<DayOfWeek> convertArrayToDayOfWeekSet(Integer[] array) {
        if (array == null || array.length == 0) {
            return new HashSet<>();
        }

        Set<DayOfWeek> result = new HashSet<>();
        for (Integer dayValue : array) {
            if (dayValue != null && dayValue >= 1 && dayValue <= 7) {
                result.add(DayOfWeek.of(dayValue)); // 1 = MONDAY, 7 = SUNDAY
            }
        }
        return result;
    }

    /**
     * Convertit Set<DayOfWeek> vers Integer[] (1-7)
     */
    private Integer[] convertDayOfWeekSetToArray(Set<DayOfWeek> weekDays) {
        if (weekDays == null || weekDays.isEmpty()) {
            return new Integer[0];
        }

        return weekDays.stream()
                .map(DayOfWeek::getValue) // MONDAY = 1, SUNDAY = 7
                .sorted()
                .toArray(Integer[]::new);
    }

    // ========== Méthodes pour les listes ==========

    /**
     * Convertit une liste d'entités vers le domaine
     */
    public List<AvailabilityRule> toDomainList(List<AvailabilityRuleEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(this::toDomain)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Convertit une liste du domaine vers des entités
     */
    public List<AvailabilityRuleEntity> toEntityList(List<AvailabilityRule> domains) {
        if (domains == null) {
            return new ArrayList<>();
        }
        return domains.stream()
                .map(this::toEntity)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}