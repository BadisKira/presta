package com.presta.infrastructure.web.controllers.user;

import com.presta.domain.model.Contractor;
import com.presta.domain.port.out.ContractorRepositoryPort;
import com.presta.infrastructure.web.dtos.contractor.ContractorDto;
import com.presta.infrastructure.web.dtos.contractor.UpdateContractorRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/contractors")
public class ContractorController {

    private final ContractorRepositoryPort contractorRepositoryPort;

    public ContractorController(ContractorRepositoryPort contractorRepositoryPort) {
        this.contractorRepositoryPort = contractorRepositoryPort;
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
        ContractorRepositoryPort.ContractorSearchCriteria criteria =
                new ContractorRepositoryPort.ContractorSearchCriteria(
                        name, speciality, page, size, sortBy, sortDirection
                );

        Page<Contractor> contractors = contractorRepositoryPort.searchContractors(criteria);
        return ResponseEntity.ok(contractors);
    }


    @PatchMapping("/{id}")
    //@PreAuthorize("hasRole('CONTRACTOR') and #id == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<?> updateContractor(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateContractorRequest request) {

        var updatedContractor = contractorRepositoryPort.updateContractor(
                id,
                request.address(),
                request.assignmentId(),
                request.speciality()
        );

        return ResponseEntity.ok(Map.of(
                "message", "Contractor mis à jour avec succès",
                "contractor", ContractorDto.fromDomain(updatedContractor)
        ));
    }

}