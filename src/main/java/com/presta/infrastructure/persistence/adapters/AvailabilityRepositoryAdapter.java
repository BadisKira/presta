package com.presta.infrastructure.persistence.adapters;

import com.presta.domain.model.AvailabilityRule;
import com.presta.domain.port.AvailabilityRuleRepositoryPort;
import com.presta.infrastructure.persistence.entities.AvailabilityRuleEntity;
import com.presta.infrastructure.persistence.mapper.AvailabilityRuleMapper;
import com.presta.infrastructure.persistence.repositories.JpaAvailabilityRuleRepository;
import com.presta.infrastructure.persistence.repositories.JpaBreakTimeRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public class AvailabilityRepositoryAdapter implements AvailabilityRuleRepositoryPort {

    private final JpaAvailabilityRuleRepository jpaAvailabilityRuleRepository;
    private final JpaBreakTimeRepository jpaBreakTimeRepository;
    private final AvailabilityRuleMapper availabilityRuleMapper;


    public AvailabilityRepositoryAdapter(JpaAvailabilityRuleRepository jpaAvailabilityRuleRepository,
                                         JpaBreakTimeRepository jpaBreakTimeRepository, AvailabilityRuleMapper availabilityRuleMapper){
           this.jpaAvailabilityRuleRepository = jpaAvailabilityRuleRepository;
           this.jpaBreakTimeRepository = jpaBreakTimeRepository;
        this.availabilityRuleMapper = availabilityRuleMapper;
    }

    @Override
    public List<AvailabilityRule> findActiveByContractorId(UUID id) {
        return this.availabilityRuleMapper.toDomainList(
                this.jpaAvailabilityRuleRepository.findByIsActiveAndContractorId(id)
        );
    }

    @Override
    public AvailabilityRule save(AvailabilityRule availabilityRule) {
        AvailabilityRuleEntity availabilityRuleEntity = this.availabilityRuleMapper.toEntity(availabilityRule);
        System.out.println(availabilityRuleEntity.toString());
        return this.availabilityRuleMapper.toDomain(
                this.jpaAvailabilityRuleRepository.save(availabilityRuleEntity)
        );
    }

    @Override
    public Optional<AvailabilityRule> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public void deleteById(UUID id) {

    }

}
