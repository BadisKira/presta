package com.presta.domain.port.in.availability;

import com.presta.domain.model.Appointment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentQueryPort {

    List<Appointment> findActiveByContractorIdAndPeriod(UUID id , LocalDateTime start , LocalDateTime endDate);
}
