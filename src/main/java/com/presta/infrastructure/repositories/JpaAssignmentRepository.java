package com.presta.infrastructure.repositories;

import com.presta.infrastructure.entities.AssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaAssignmentRepository extends JpaRepository<AssignmentEntity, UUID> {
}
