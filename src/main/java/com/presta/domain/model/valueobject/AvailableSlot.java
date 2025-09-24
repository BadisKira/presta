package com.presta.domain.model.valueobject;

import java.util.UUID;

public record AvailableSlot(
        UUID contractorId,
        TimeSlot timeSlot,
        AvailabilityStatus status
) {}