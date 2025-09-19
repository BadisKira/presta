package com.presta.application.usecases;

import com.presta.domain.model.Assignment;
import com.presta.domain.model.Contractor;
import com.presta.domain.port.in.contractor.ContractorQueryPort;
import com.presta.domain.port.out.AssignmentPort;
import com.presta.domain.port.out.UserRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

@Component
public class ContractorQueryUseCase implements ContractorQueryPort {

    private final UserRepositoryPort userRepositoryPort;
    private final AssignmentPort assignmentPort;

    public ContractorQueryUseCase(UserRepositoryPort userRepositoryPort, AssignmentPort assignmentPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.assignmentPort = assignmentPort;
    }

    @Override
    public Page<Contractor> searchContractors(ContractorSearchCriteria criteria) {
        // Création de la pagination et tri
        Sort sort = createSort(criteria.sortBy(), criteria.sortDirection());
        Pageable pageable = PageRequest.of(criteria.page(), criteria.size(), sort);

        // Appel du repository avec les filtres
        return userRepositoryPort.findContractors(
                criteria.name(),
                criteria.speciality(),
                pageable
        );
    }


    @Override
    public Contractor updateContractor(UUID id, String address, UUID assignmentId, String speciality) {
        Contractor existingContractor = userRepositoryPort.findContractorById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contractor non trouvé avec l'ID: " + id));

        if (assignmentId != null && !assignmentPort.exists(assignmentId)) {
            throw new IllegalArgumentException("Assignment non trouvé avec l'ID: " + assignmentId);
        }


        Assignment assignment = assignmentPort.findById(assignmentId).get();


        Contractor updatedContractor = new Contractor(
                existingContractor.id(),
                existingContractor.user(),
                existingContractor.fullName(),
                address != null ? address : existingContractor.address(),
                assignment,
                speciality != null ? speciality : existingContractor.speciality()
        );

        return userRepositoryPort.saveContractor(updatedContractor);
    }
    private Sort createSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "id"; // Tri par défaut
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return Sort.by(direction, sortBy);
    }
}
