package com.presta.infrastructure.persistence.adapters.availability;

import com.presta.domain.model.UnavailabilityRule;
import com.presta.domain.port.out.UnavailabilityRuleRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
@Transactional
public class UnavailabilityRepositoryAdapter implements UnavailabilityRuleRepositoryPort {
    @Override
    public List<UnavailabilityRule> findByContractorIdAndPeriod(UUID id, LocalDate startDate, LocalDate endDate) {
        return List.of();
    }

    @Override
    public UnavailabilityRule save(UnavailabilityRule rule) {
        return null;
    }

    @Override
    public Optional<UnavailabilityRule> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public void deleteById(UUID id) {

    }
}
