package com.presta.infrastructure.web.dtos.availability;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.presta.domain.model.UnavailabilityRule;
import com.presta.infrastructure.persistence.entities.UnavailabilityRuleEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record CreateUnavailabilityRuleRequest(

        @NotNull(message = "L'ID du prestataire est obligatoire")
        UUID contractorId,

        @NotNull(message = "La date de début est obligatoire")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate startDate,

        @NotNull(message = "La date de fin est obligatoire")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate endDate,

        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime startTime,

        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime endTime,

        @Size(max = 500, message = "La raison ne peut dépasser 500 caractères")
        String reason
) {



    /**
     * Convertit un DTO de création vers un objet domain
     */
    public UnavailabilityRule toDomain() {


        return UnavailabilityRule.create(
                null,
                this.contractorId(),
                this.startDate(),
                this.endDate(),
                this.startTime(),
                this.endTime(),
                this.reason()
        );
    }

    /**
     * Convertit un DTO de création directement vers une entité JPA
     */
    public UnavailabilityRuleEntity toEntity() {

        return new UnavailabilityRuleEntity(
                null,
                this.contractorId(),
                this.startDate(),
                this.endDate(),
                this.startTime(),
                this.endTime(),
                this.reason(),
                LocalDateTime.now()
        );
    }
}