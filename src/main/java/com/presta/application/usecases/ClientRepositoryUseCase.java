package com.presta.application.usecases;

import com.presta.domain.model.Client;
import com.presta.domain.port.ClientRepositoryPort;
import com.presta.domain.port.UserRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ClientRepositoryUseCase implements ClientRepositoryPort {

    private final UserRepositoryPort userRepositoryPort;

    public ClientRepositoryUseCase(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public Page<Client> searchClients(ClientSearchCriteria criteria) {
        Sort sort = createSort(criteria.sortBy(), criteria.sortDirection());
        Pageable pageable = PageRequest.of(criteria.page(), criteria.size(), sort);

        return userRepositoryPort.findClients(criteria.name(), pageable);
    }

    private Sort createSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "id";
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return Sort.by(direction, sortBy);
    }
    @Override
    public Optional<Client> findById(UUID id) {
        return this.userRepositoryPort.findClientById(id);
    }


}