package com.presta.infrastructure.persistence.repositories;

import com.presta.infrastructure.persistence.entities.AvailabilityRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface JpaAvailabilityRuleRepository extends JpaRepository<AvailabilityRuleEntity,UUID> , JpaSpecificationExecutor<AvailabilityRuleEntity> {

    @Query(""" 
            SELECT a
            FROM AvailabilityRuleEntity a,
                 ContractorEntity c , UserEntity u
            WHERE a.isActive = true AND ( c.id = :contractorId AND c.id = u.id and u.isActive = true )
    """)
    List<AvailabilityRuleEntity> findByIsActiveAndContractorId(UUID contractorId);
}
