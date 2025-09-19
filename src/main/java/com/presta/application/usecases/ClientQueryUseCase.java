package com.presta.application.usecases;

import com.presta.domain.model.Client;
import com.presta.domain.port.in.client.ClientQueryPort;
import com.presta.domain.port.out.UserRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class ClientQueryUseCase implements ClientQueryPort {

    private final UserRepositoryPort userRepositoryPort;

    public ClientQueryUseCase(UserRepositoryPort userRepositoryPort) {
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
}