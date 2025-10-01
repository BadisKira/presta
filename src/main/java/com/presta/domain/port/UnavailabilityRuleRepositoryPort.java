package com.presta.domain.port;

import com.presta.domain.model.UnavailabilityRule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UnavailabilityRuleRepositoryPort {
    List<UnavailabilityRule> findByContractorIdAndDateRange(UUID id , LocalDate startDate, LocalDate endDate);
    UnavailabilityRule save(UnavailabilityRule rule);
    Optional<UnavailabilityRule> findById(UUID id);
    void deleteById(UUID id);
}


