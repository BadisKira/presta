package com.presta.infrastructure.web.controllers.user;

import com.presta.domain.exception.UserNotFoundException;
import com.presta.domain.model.Contractor;
import com.presta.domain.model.ContractorPlanning;
import com.presta.application.usecases.SchedulingUseCase;
import com.presta.domain.port.ContractorRepositoryPort;
import com.presta.domain.port.UserAuthenticationPort;
import com.presta.domain.port.UserRepositoryPort;
import com.presta.infrastructure.web.dtos.contractor.ContractorDto;
import com.presta.infrastructure.web.dtos.contractor.UpdateContractorRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/contractors")
public class ContractorController {

    private final ContractorRepositoryPort contractorRepositoryPort;
    private final UserAuthenticationPort authPort;
    private final UserRepositoryPort userRepositoryPort;
    private final SchedulingUseCase schedulingUseCase;

    public ContractorController(ContractorRepositoryPort contractorRepositoryPort, UserAuthenticationPort authPort, UserRepositoryPort userRepositoryPort, SchedulingUseCase schedulingUseCase) {
        this.contractorRepositoryPort = contractorRepositoryPort;
        this.authPort = authPort;
        this.userRepositoryPort = userRepositoryPort;
        this.schedulingUseCase = schedulingUseCase;
    }

    @GetMapping
    public ResponseEntity<Page<Contractor>> getContractors(
            @RequestParam(required = false , name = "name") String name,
            @RequestParam(required = false,name = "speciality") String speciality,
            @RequestParam(required = false,name = "assignmentId") String assignmentId,
            @RequestParam(required = false,name = "address") String address,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        ContractorRepositoryPort.ContractorSearchCriteria criteria =
                new ContractorRepositoryPort.ContractorSearchCriteria(
                        name, speciality, assignmentId, address, page, size, sortBy, sortDirection
                );

        Page<Contractor> contractors = contractorRepositoryPort.searchContractors(criteria);
        return ResponseEntity.ok(contractors);
    }


    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ContractorDto> updateContractor(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateContractorRequest request) {

        var updatedContractor = contractorRepositoryPort.updateContractor(
                id,
                request.address(),
                request.assignmentId(),
                request.speciality()
        );

        return ResponseEntity.ok(ContractorDto.fromDomain(updatedContractor));
    }

    @GetMapping(value = "/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ContractorDto> getMe() {
        var authUser = authPort.getCurrentAuthenticatedUser()
                .orElseThrow(() -> new AccessDeniedException("Not authenticated"));

        var user = userRepositoryPort.findUserByKeycloakId(authUser.keycloakId())
                .orElseThrow(() -> new UserNotFoundException(authUser.keycloakId().getValue()));

        if (!user.isActive()) {
            throw new IllegalStateException("Le compte du prestataire n'est pas activé");
        }

        var contractor = userRepositoryPort.findContractorById(user.id())
                .orElseThrow(() -> new UserNotFoundException(user.id()));

        return ResponseEntity.ok(ContractorDto.fromDomain(contractor));
    }

    @GetMapping("/{id}/plannings")
    public ResponseEntity<ContractorPlanning> contractorPlanningResponse(
            @PathVariable UUID id ,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
            ){
        ContractorPlanning contractorPlanning = this.schedulingUseCase.generatePlanning(id,startDate,endDate);
        return ResponseEntity.ok(contractorPlanning);
    }
}