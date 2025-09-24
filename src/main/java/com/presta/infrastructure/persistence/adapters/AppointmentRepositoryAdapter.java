package com.presta.infrastructure.persistence.adapters;

import com.presta.domain.exception.AppointmentNotFoundException;
import com.presta.domain.model.Appointment;
import com.presta.domain.model.valueobject.AppointmentStatus;
import com.presta.domain.port.AppointmentRepositoryPort;
import com.presta.infrastructure.persistence.entities.AppointmentEntity;
import com.presta.infrastructure.persistence.mapper.AppointmentMapper;
import com.presta.infrastructure.persistence.repositories.JpaAppointmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Transactional
public class AppointmentRepositoryAdapter implements AppointmentRepositoryPort {

    private final JpaAppointmentRepository jpaRepository;
    private final AppointmentMapper mapper;

    public AppointmentRepositoryAdapter(JpaAppointmentRepository jpaRepository,
                                        AppointmentMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Appointment save(Appointment appointment) {
        AppointmentEntity entity;
        if(appointment.getId() == null) {
            entity = mapper.toEntity(appointment);
        }else {
            Optional<AppointmentEntity> existingEntity =
                    jpaRepository.findById(appointment.getId());

            if (existingEntity.isPresent()) {
                entity = existingEntity.get();
                mapper.updateEntity(entity, appointment);
            }else {
                throw new AppointmentNotFoundException("Rendez-vous inéxistant ");
            }

        }

        AppointmentEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Appointment> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Appointment> findByClientId(UUID clientId) {
        return jpaRepository.findByClientId(clientId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Appointment> findByContractorId(UUID contractorId) {
        return jpaRepository.findByContractorId(contractorId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Appointment> findByContractorIdAndDateRange(
            UUID contractorId,
            LocalDateTime start,
            LocalDateTime end) {

        return jpaRepository.findByContractorIdAndAppointmentDateTimeBetween(
                        contractorId, start, end)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Appointment> findActiveByContractorIdAndPeriod(
            UUID contractorId,
            LocalDateTime start,
            LocalDateTime end) {

        // Récupérer uniquement les RDV avec statut PENDING ou CONFIRMED
        List<String> activeStatuses = List.of(
                AppointmentStatus.PENDING.name(),
                AppointmentStatus.CONFIRMED.name()
        );

        return jpaRepository.findByContractorIdAndAppointmentDateTimeBetweenAndStatusIn(
                        contractorId, start, end, activeStatuses)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByContractorAndDateTime(UUID contractorId, LocalDateTime dateTime) {
        return jpaRepository.existsByContractorIdAndAppointmentDateTime(
                contractorId, dateTime
        );
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}

