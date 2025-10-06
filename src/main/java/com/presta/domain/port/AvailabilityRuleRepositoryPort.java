package com.presta.domain.port;

import com.presta.domain.model.AvailabilityRule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvailabilityRuleRepositoryPort {
    List<AvailabilityRule> findActiveByContractorId(UUID id);
    AvailabilityRule save(AvailabilityRule availabilityRule);
    Optional<AvailabilityRule> findById(UUID id);
    void deleteById(UUID id);
}