package com.presta.infrastructure.web.controllers.user;

import com.presta.domain.model.Client;
import com.presta.domain.port.in.client.ClientQueryPort;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientQueryPort clientQueryPort;

    public ClientController(ClientQueryPort clientQueryPort) {
        this.clientQueryPort = clientQueryPort;
    }

    @GetMapping
    public ResponseEntity<Page<Client>> getClients(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        ClientQueryPort.ClientSearchCriteria criteria =
                new ClientQueryPort.ClientSearchCriteria(
                        name, page, size, sortBy, sortDirection
                );

        Page<Client> clients = clientQueryPort.searchClients(criteria);
        return ResponseEntity.ok(clients);
    }
}
