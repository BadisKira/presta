package com.presta.infrastructure.persistence.repositories;

import com.presta.infrastructure.persistence.entities.BreakTimeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface JpaBreakTimeRepository extends JpaRepository<BreakTimeEntity, UUID> , JpaSpecificationExecutor<BreakTimeEntity> {
}
