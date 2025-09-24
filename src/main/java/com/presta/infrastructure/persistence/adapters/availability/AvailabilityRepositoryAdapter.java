package com.presta.infrastructure.persistence.adapters.availability;

import com.presta.domain.model.AvailabilityRule;
import com.presta.domain.port.in.availability.AvailabilityQueryPort;
import com.presta.infrastructure.persistence.entities.AvailabilityRuleEntity;
import com.presta.infrastructure.persistence.mapper.availability.AvailabilityRuleMapper;
import com.presta.infrastructure.persistence.repositories.availability.JpaAvailabilityRuleRepository;
import com.presta.infrastructure.persistence.repositories.availability.JpaBreakTimeRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class AvailabilityRepositoryAdapter implements AvailabilityQueryPort {

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

}
