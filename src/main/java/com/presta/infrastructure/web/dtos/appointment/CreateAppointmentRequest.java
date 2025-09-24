package com.presta.infrastructure.web.dtos.appointment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.presta.domain.model.Appointment;
import com.presta.domain.model.valueobject.AppointmentStatus;
import com.presta.infrastructure.persistence.entities.AppointmentEntity;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateAppointmentRequest(

        @NotNull(message = "L'ID du prestataire est obligatoire")
        UUID contractorId,

        @NotNull(message = "L'ID du client est obligatoire")
        UUID clientId,

        @NotNull(message = "La date et l'heure du rendez-vous sont obligatoires")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime appointmentDateTime,

        @NotNull(message = "La durée est obligatoire")
        @Positive(message = "La durée doit être positive")
        @Min(value = 15, message = "La durée minimale est de 15 minutes")
        @Max(value = 480, message = "La durée maximale est de 8 heures (480 minutes)")
        Integer duration,

        @Size(max = 500, message = "La raison ne peut dépasser 500 caractères")
        String reason,

        @Size(max = 1000, message = "Les notes ne peuvent dépasser 1000 caractères")
        String notes
) {

    public AppointmentEntity toEntity() {


        AppointmentEntity entity = new AppointmentEntity();
        entity.setId(UUID.randomUUID());
        entity.setContractorId(this.contractorId());
        entity.setClientId(this.clientId());
        entity.setAppointmentDateTime(this.appointmentDateTime());
        entity.setDuration(this.duration());
        entity.setStatus(AppointmentStatus.PENDING);
        entity.setReason(this.reason());
        entity.setNotes(this.notes());

        return entity;
    }


    public Appointment toDomain() {
        return Appointment.create(
                null,
                this.clientId(),
                this.contractorId(),
                this.appointmentDateTime(),
                this.duration(),
                this.reason(),
                this.notes()
        );
    }
}