package com.presta.domain.port.in.client;

import com.presta.domain.model.Client;
import org.springframework.data.domain.Page;

public interface ClientQueryPort {

    record ClientSearchCriteria(
            String name,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {}

    Page<Client> searchClients(ClientSearchCriteria criteria);
}