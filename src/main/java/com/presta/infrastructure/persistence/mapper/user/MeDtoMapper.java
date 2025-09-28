package com.presta.infrastructure.persistence.mapper.user;


import com.presta.domain.model.Client;
import com.presta.domain.model.Contractor;
import com.presta.domain.port.in.UserSyncPort;
import com.presta.infrastructure.persistence.entities.ClientEntity;
import com.presta.infrastructure.persistence.entities.ContractorEntity;
import com.presta.infrastructure.web.dtos.contractor.ContractorDto;
import com.presta.infrastructure.web.dtos.user.ClientDto;
import com.presta.infrastructure.web.dtos.user.MeDto;
import com.presta.infrastructure.web.dtos.user.UserDto;
import org.springframework.stereotype.Component;

@Component
public class MeDtoMapper{
    public static  MeDto toDto(UserSyncPort.UserRole role ,
                               Client client,
                               Contractor contractor
                               ) {
        return switch (role) {
            case CLIENT -> new ClientDto(
                    client.id(),
                    UserDto.fromDomain(client.user())
            );
            case CONTRACTOR -> new ContractorDto(
                    contractor.id(),
                    contractor.fullName(),
                    contractor.address(),
                    contractor.assignment(),
                    contractor.speciality(),
                    UserDto.fromDomain(contractor.user())
            );
        };
    }
}

