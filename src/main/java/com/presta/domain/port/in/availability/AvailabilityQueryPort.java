package com.presta.domain.port.in.availability;

import com.presta.domain.model.AvailabilityRule;

import java.util.List;
import java.util.UUID;

public interface AvailabilityQueryPort{
    List<AvailabilityRule> findActiveByContractorId(UUID id);
    AvailabilityRule save(AvailabilityRule availabilityRule);
}
