package com.presta.infrastructure.web.dtos.contractor;
import com.presta.domain.model.Assignment;
import com.presta.domain.model.Contractor;
import com.presta.infrastructure.web.dtos.user.UserDto;

import java.util.UUID;

public record ContractorDto(
        UUID id,
        String fullName,
        String address,
        Assignment assignment,
        String speciality,
        UserDto user
) {

    public static ContractorDto fromDomain(Contractor contractor) {
        return new ContractorDto(
                contractor.id(),
                contractor.fullName(),
                contractor.address(),
                contractor.assignment(),
                contractor.speciality(),
                UserDto.fromDomain(contractor.user())
        );
    }
}