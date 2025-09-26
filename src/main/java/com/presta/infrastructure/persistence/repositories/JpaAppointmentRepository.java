package com.presta.infrastructure.persistence.repositories;

import com.presta.domain.model.Appointment;
import com.presta.infrastructure.persistence.entities.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface JpaAppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {

    List<AppointmentEntity> findByClientId(UUID clientId);

    List<AppointmentEntity> findByContractorId(UUID contractorId);

    List<AppointmentEntity> findByContractorIdAndAppointmentDateTimeBetween(
            UUID contractorId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("SELECT a FROM AppointmentEntity a " +
            "WHERE a.contractorId = :contractorId " +
            "AND a.appointmentDateTime BETWEEN :start AND :end " +
            "AND a.status IN :statuses")
    List<AppointmentEntity> findByContractorIdAndAppointmentDateTimeBetweenAndStatusIn(
            @Param("contractorId") UUID contractorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("statuses") List<String> statuses
    );

    boolean existsByContractorIdAndAppointmentDateTime(
            UUID contractorId,
            LocalDateTime appointmentDateTime
    );

    @Query("SELECT COUNT(a) > 0 FROM AppointmentEntity a " +
            "WHERE a.contractorId = :contractorId " +
            "AND a.appointmentDateTime = :dateTime " +
            "AND a.status IN ('PENDING', 'CONFIRMED')")
    boolean existsActiveAppointmentAt(
            @Param("contractorId") UUID contractorId,
            @Param("dateTime") LocalDateTime dateTime
    );
}