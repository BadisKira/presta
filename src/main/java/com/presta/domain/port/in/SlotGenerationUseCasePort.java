package com.presta.domain.port.in;


import com.presta.domain.model.valueobject.AvailableSlot;
import com.presta.domain.model.valueobject.TimeSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;



public interface SlotGenerationUseCasePort {
    record GenerateAvailableSlotsCommand(
            UUID contractorId,
            LocalDate startDate,
            LocalDate endDate) {
    }
    record CheckSlotAvailabilityCommand(
            UUID contractorId,
            LocalDateTime startDateTime,
            int duration) {
    }
    record AvailableSlotsQuery(
            UUID contractorId,
            LocalDate startDate,
            LocalDate endDate) {
    }

    List<AvailableSlot> generateAvailableSlots(GenerateAvailableSlotsCommand command);
    List<TimeSlot> findOnlyAvailableSlots(AvailableSlotsQuery query);
    boolean isSlotAvailable(CheckSlotAvailabilityCommand command);
}