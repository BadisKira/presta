package com.presta.infrastructure.persistence.repositories;

import com.presta.infrastructure.persistence.entities.ContractorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaContractorRepository extends JpaRepository<ContractorEntity, UUID> {

    List<ContractorEntity> findByAssignmentId(UUID assignmentId);

    List<ContractorEntity> findBySpeciality(String speciality);
}
