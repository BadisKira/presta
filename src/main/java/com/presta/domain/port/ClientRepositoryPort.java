package com.presta.domain.port;

import com.presta.domain.model.Client;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepositoryPort {

    record ClientSearchCriteria(
            String name,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {}

    Page<Client> searchClients(ClientSearchCriteria criteria);
    Optional<Client> findById(UUID id) ;


}