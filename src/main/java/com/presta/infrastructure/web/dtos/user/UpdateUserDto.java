package com.presta.infrastructure.web.dtos.user;

public record UpdateUserDto(
        String firstName,
        String lastName,
        Boolean enabled
) { }