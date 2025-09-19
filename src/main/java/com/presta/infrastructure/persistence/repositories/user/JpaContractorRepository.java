package com.presta.infrastructure.persistence.repositories.user;

import com.presta.domain.model.Contractor;
import com.presta.infrastructure.persistence.entities.ContractorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface JpaContractorRepository extends JpaRepository<ContractorEntity, UUID> , JpaSpecificationExecutor<ContractorEntity> {

   // List<ContractorEntity> findByAssignmentId(UUID assignmentId);

    // List<ContractorEntity> findBySpeciality(String speciality);


}
