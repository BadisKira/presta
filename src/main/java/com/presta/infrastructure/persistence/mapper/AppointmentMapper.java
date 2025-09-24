package com.presta.infrastructure.persistence.mapper;

import com.presta.domain.model.Appointment;
import com.presta.domain.model.valueobject.AppointmentDetails;
import com.presta.domain.model.valueobject.AppointmentStatus;
import com.presta.domain.model.valueobject.TimeSlot;
import com.presta.infrastructure.persistence.entities.AppointmentEntity;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public Appointment toDomain(AppointmentEntity entity) {
        if (entity == null) return null;

        return new Appointment(
                entity.getId(),
                entity.getClientId(),
                entity.getContractorId(),
                new TimeSlot(entity.getAppointmentDateTime(), entity.getDuration()),
                entity.getStatus() != null ? entity.getStatus() : AppointmentStatus.PENDING,
                new AppointmentDetails(entity.getReason(), entity.getNotes()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public AppointmentEntity toEntity(Appointment domain) {
        if (domain == null) return null;

        AppointmentEntity entity = new AppointmentEntity();
        entity.setId(domain.getId());
        entity.setClientId(domain.getClientId());
        entity.setContractorId(domain.getContractorId());
        entity.setAppointmentDateTime(domain.getAppointmentDateTime());
        entity.setDuration(domain.getDuration());
        entity.setStatus(domain.getStatus());
        entity.setReason(domain.getReason());
        entity.setNotes(domain.getNotes());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        return entity;
    }

    public void updateEntity(AppointmentEntity entity, Appointment domain) {
        if (entity == null || domain == null) return;

        entity.setClientId(domain.getClientId());
        entity.setContractorId(domain.getContractorId());
        entity.setAppointmentDateTime(domain.getAppointmentDateTime());
        entity.setDuration(domain.getDuration());
        entity.setStatus(domain.getStatus());
        entity.setReason(domain.getReason());
        entity.setNotes(domain.getNotes());
        entity.setUpdatedAt(domain.getUpdatedAt());
    }
}