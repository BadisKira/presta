package com.presta.domain.port.out;

import com.presta.domain.model.valueobject.AvailableSlot;
import com.presta.domain.model.valueobject.TimeSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SlotGeneratorPort {

    List<AvailableSlot> generateAvailableSlots(
            UUID contractorId,
            LocalDate startDate,
            LocalDate endDate);

    List<TimeSlot> findOnlyAvailableSlots(
            UUID contractorId,
            LocalDate startDate,
            LocalDate endDate);


    boolean isSlotAvailable(
            UUID contractorId,
            LocalDateTime startDateTime,
            int duration);
}