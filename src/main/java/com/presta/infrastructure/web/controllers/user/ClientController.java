package com.presta.infrastructure.web.controllers.user;

import com.presta.domain.exception.UserNotFoundException;
import com.presta.domain.model.Client;
import com.presta.domain.port.ClientRepositoryPort;
import com.presta.domain.port.UserAuthenticationPort;
import com.presta.domain.port.UserRepositoryPort;
import com.presta.infrastructure.web.dtos.user.ClientDto;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientRepositoryPort clientRepositoryPort;
    private final UserAuthenticationPort authPort;
    private final UserRepositoryPort userRepositoryPort;

    public ClientController(ClientRepositoryPort clientRepositoryPort, UserAuthenticationPort authPort, UserRepositoryPort userRepositoryPort) {
        this.clientRepositoryPort = clientRepositoryPort;
        this.authPort = authPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @GetMapping
    public ResponseEntity<Page<Client>> getClients(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        ClientRepositoryPort.ClientSearchCriteria criteria =
                new ClientRepositoryPort.ClientSearchCriteria(
                        name, page, size, sortBy, sortDirection
                );

        Page<Client> clients = clientRepositoryPort.searchClients(criteria);
        return ResponseEntity.ok(clients);
    }

    @GetMapping(value = "/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClientDto> getMe() {
        var authUser = authPort.getCurrentAuthenticatedUser()
                .orElseThrow(() -> new AccessDeniedException("Not authenticated"));

        var user = userRepositoryPort.findUserByKeycloakId(authUser.keycloakId())
                .orElseThrow(() -> new UserNotFoundException(authUser.keycloakId().getValue()));

        if (!user.isActive()) {
            throw new IllegalStateException("Le compte du client n'est pas activé");
        }

        var client = userRepositoryPort.findClientById(user.id())
                .orElseThrow(() -> new UserNotFoundException(user.id()));

        return ResponseEntity.ok(ClientDto.fromDomain(client));
    }

}
