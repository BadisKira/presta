package com.presta.domain.port.in.contractor;

import com.presta.domain.model.Assignment;
import com.presta.domain.model.Contractor;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ContractorQueryPort {

    record ContractorSearchCriteria(
            String name,
            String speciality,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {}

    Page<Contractor> searchContractors(ContractorSearchCriteria criteria);
    Contractor updateContractor( UUID contractorId , String address , UUID assignmentId , String speciality);
    }

