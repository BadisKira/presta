package com.presta.domain.port;

import com.presta.domain.model.Contractor;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface ContractorRepositoryPort {


    record ContractorSearchCriteria(
            String name,
            String speciality,
            String assignmentId,    // NOUVEAU
            String address,         // NOUVEAU
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {}

    Page<Contractor> searchContractors(ContractorSearchCriteria criteria);
    Contractor updateContractor( UUID contractorId , String address , UUID assignmentId , String speciality);
    boolean isActive(UUID contractorId);
    Optional<Contractor> findById(UUID id);


    }

