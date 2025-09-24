package com.presta.infrastructure.web.dtos.availability;

import com.presta.domain.model.AvailabilityRule;
import com.presta.domain.model.valueobject.SlotConfiguration;
import com.presta.domain.model.valueobject.TimeRange;

import java.time.DayOfWeek;
import java.util.Set;
import java.util.UUID;


public record CreateAvailabilityRuleRequest(
        UUID contractorId,
        Set<DayOfWeek> weekDays,
        TimeRange timeRange,
        SlotConfiguration slotConfig
) {
    public AvailabilityRule toDomain() {
        return AvailabilityRule.create(
                contractorId,
                weekDays,
                timeRange,
                slotConfig
        );
    }
}