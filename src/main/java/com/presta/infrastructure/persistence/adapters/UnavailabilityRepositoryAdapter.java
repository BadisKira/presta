package com.presta.infrastructure.persistence.adapters;

import com.presta.domain.model.UnavailabilityRule;
import com.presta.domain.port.UnavailabilityRuleRepositoryPort;
import com.presta.infrastructure.persistence.entities.UnavailabilityRuleEntity;
import com.presta.infrastructure.persistence.mapper.UnavailabilityRuleMapper;
import com.presta.infrastructure.persistence.repositories.JpaUnavailabilityRuleRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
@Transactional
public class UnavailabilityRepositoryAdapter implements UnavailabilityRuleRepositoryPort {

    private final JpaUnavailabilityRuleRepository jpaUnavailabilityRuleRepository ;
    private final UnavailabilityRuleMapper unavailabilityRuleMapper;

    public UnavailabilityRepositoryAdapter(JpaUnavailabilityRuleRepository jpaUnavailabilityRuleRepository, UnavailabilityRuleMapper unavailabilityRuleMapper) {
        this.jpaUnavailabilityRuleRepository = jpaUnavailabilityRuleRepository;
        this.unavailabilityRuleMapper = unavailabilityRuleMapper;
    }

    @Override
    public List<UnavailabilityRule> findByContractorIdAndDateRange(UUID id, LocalDate startDate, LocalDate endDate) {
        return  this.jpaUnavailabilityRuleRepository.findByContractorIdAndDateRange(id,startDate,endDate).stream()
                .map(this.unavailabilityRuleMapper::toDomain)
                .toList();
    }

    @Override
    public UnavailabilityRule save(UnavailabilityRule rule) {
        UnavailabilityRuleEntity unavailabilityRuleEntity = this.unavailabilityRuleMapper.toEntity(rule);
        UnavailabilityRuleEntity entity = this.jpaUnavailabilityRuleRepository.save(unavailabilityRuleEntity);
        return this.unavailabilityRuleMapper.toDomain(entity);
    }

    @Override
    public Optional<UnavailabilityRule> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public void deleteById(UUID id) {

    }
}
