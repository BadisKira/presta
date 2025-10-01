package com.presta.domain.port;

import com.presta.domain.model.Appointment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepositoryPort {
    Appointment save(Appointment appointment);
    Optional<Appointment> findById(UUID id);
    List<Appointment> findByClientId(UUID clientId);
    List<Appointment> findByContractorId(UUID contractorId);
    List<Appointment> findByContractorIdAndDateRange(
            UUID contractorId,
            LocalDateTime start,
            LocalDateTime end
    );
    List<Appointment> findActiveByContractorIdAndPeriod(
            UUID contractorId,
            LocalDateTime start,
            LocalDateTime end
    );

    boolean existsByContractorAndDateTime(UUID contractorId, LocalDateTime dateTime);
    void deleteById(UUID id);


}
