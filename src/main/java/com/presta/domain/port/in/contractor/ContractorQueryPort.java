package com.presta.domain.port.in.contractor;

import com.presta.domain.model.Contractor;
import org.springframework.data.domain.Page;

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
}

