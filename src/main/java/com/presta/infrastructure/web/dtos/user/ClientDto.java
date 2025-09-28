package com.presta.infrastructure.web.dtos.user;

import com.presta.domain.model.Client;

import java.util.UUID;

public record ClientDto(UUID id, UserDto user) implements MeDto {
    public static ClientDto fromDomain(Client client) {
        return new ClientDto(
                client.id(),
                UserDto.fromDomain(client.user())
        );
    }
}
