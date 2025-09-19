package com.presta.infrastructure.web.controllers.user;

import com.presta.domain.model.Contractor;
import com.presta.domain.port.in.contractor.ContractorQueryPort;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contractors")
public class ContractorController {

    private final ContractorQueryPort contractorQueryPort;

    public ContractorController(ContractorQueryPort contractorQueryPort) {
        this.contractorQueryPort = contractorQueryPort;
    }

    @GetMapping
    public ResponseEntity<Page<Contractor>> getContractors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String speciality,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        ContractorQueryPort.ContractorSearchCriteria criteria =
                new ContractorQueryPort.ContractorSearchCriteria(
                        name, speciality, page, size, sortBy, sortDirection
                );

        Page<Contractor> contractors = contractorQueryPort.searchContractors(criteria);
        return ResponseEntity.ok(contractors);
    }
}