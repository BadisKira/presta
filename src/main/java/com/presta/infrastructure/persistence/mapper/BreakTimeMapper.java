package com.presta.infrastructure.persistence.mapper;

import com.presta.domain.model.BreakTime;
import com.presta.domain.model.valueobject.TimeRange;
import com.presta.infrastructure.persistence.entities.BreakTimeEntity;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mapper bidirectionnel entre BreakTime (domaine) et BreakTimeEntity (JPA)
 */
@Component
public class BreakTimeMapper {

    /**
     * Entity → Domain
     * Convertit une entité JPA vers le modèle du domaine
     */
    public BreakTime toDomain(BreakTimeEntity entity) {
        if (entity == null) {
            return null;
        }

        // Reconstituer le Value Object TimeRange
        TimeRange timeRange = new TimeRange(
                entity.getStartTime(),
                entity.getEndTime()
        );

        // Convertir Integer[] vers Set<DayOfWeek> (peut être null)
        Set<DayOfWeek> weekDays = entity.getWeekDays() != null
                ? convertArrayToDayOfWeekSet(entity.getWeekDays())
                : null;

        return new BreakTime(timeRange, weekDays);
    }

    /**
     * Domain → Entity
     * Convertit le modèle du domaine vers une entité JPA
     */
    public BreakTimeEntity toEntity(BreakTime domain) {
        if (domain == null) {
            return null;
        }

        BreakTimeEntity entity = new BreakTimeEntity();

        // Décomposer le Value Object TimeRange
        entity.setStartTime(domain.timeRange().startTime());
        entity.setEndTime(domain.timeRange().endTime());

        // Convertir Set<DayOfWeek> vers Integer[] (peut être null)
        entity.setWeekDays(
                domain.weekDays() != null
                        ? convertDayOfWeekSetToArray(domain.weekDays())
                        : null
        );

        return entity;
    }

    /**
     * Met à jour une entité existante avec les données du domaine
     */
    public void updateEntity(BreakTimeEntity entity, BreakTime domain) {
        if (entity == null || domain == null) {
            return;
        }

        entity.setStartTime(domain.timeRange().startTime());
        entity.setEndTime(domain.timeRange().endTime());
        entity.setWeekDays(
                domain.weekDays() != null
                        ? convertDayOfWeekSetToArray(domain.weekDays())
                        : null
        );
    }

    // ========== Méthodes utilitaires de conversion ==========

    /**
     * Convertit Integer[] (1-7) vers Set<DayOfWeek>
     * @param array Array d'entiers où 1 = MONDAY, 7 = SUNDAY
     * @return Set de DayOfWeek
     */
    private Set<DayOfWeek> convertArrayToDayOfWeekSet(Integer[] array) {
        if (array == null || array.length == 0) {
            return new HashSet<>();
        }

        Set<DayOfWeek> result = new HashSet<>();
        for (Integer dayValue : array) {
            if (dayValue != null && dayValue >= 1 && dayValue <= 7) {
                result.add(DayOfWeek.of(dayValue));
            }
        }
        return result;
    }

    /**
     * Convertit Set<DayOfWeek> vers Integer[] (1-7)
     * @param weekDays Set de DayOfWeek
     * @return Array d'entiers triés où MONDAY = 1, SUNDAY = 7
     */
    private Integer[] convertDayOfWeekSetToArray(Set<DayOfWeek> weekDays) {
        if (weekDays == null || weekDays.isEmpty()) {
            return new Integer[0];
        }

        return weekDays.stream()
                .map(DayOfWeek::getValue)
                .sorted()
                .toArray(Integer[]::new);
    }

    // ========== Méthodes pour les listes ==========

    /**
     * Convertit une liste d'entités vers le domaine
     */
    public List<BreakTime> toDomainList(List<BreakTimeEntity> entities) {
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
    public List<BreakTimeEntity> toEntityList(List<BreakTime> domains) {
        if (domains == null) {
            return new ArrayList<>();
        }
        return domains.stream()
                .map(this::toEntity)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}