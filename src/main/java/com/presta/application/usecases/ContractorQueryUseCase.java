package com.presta.application.usecases;

import com.presta.domain.model.Contractor;
import com.presta.domain.port.in.contractor.ContractorQueryPort;
import com.presta.domain.port.out.UserRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class ContractorQueryUseCase implements ContractorQueryPort {

    private final UserRepositoryPort userRepositoryPort;

    public ContractorQueryUseCase(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
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
