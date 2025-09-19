package com.presta.infrastructure.web.dtos.contractor;

import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateContractorRequest(
        @Size(max = 250, message = "L'adresse ne peut pas dépasser 250 caractères")
        String address,

        UUID assignmentId,

        @Size(max = 100, message = "La spécialité ne peut pas dépasser 100 caractères")
        String speciality
) {
}