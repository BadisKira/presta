package com.presta.application.usecases;


import com.presta.domain.exception.AppointmentNotFoundException;
import com.presta.domain.exception.ContractorNotActiveException;
import com.presta.domain.exception.SlotNotAvailableException;
import com.presta.domain.exception.UnauthorizedAppointmentActionException;
import com.presta.domain.model.Appointment;
import com.presta.domain.model.valueobject.AppointmentStatus;
import com.presta.domain.port.*;
import com.presta.domain.model.valueobject.TimeSlot;

import com.presta.domain.service.AppointmentDomainService;
import com.presta.infrastructure.persistence.adapters.AvailabilityRepositoryAdapter;
import com.presta.infrastructure.persistence.repositories.JpaAvailabilityRuleRepository;
import com.presta.infrastructure.web.dtos.appointment.CreateAppointmentRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Component
public class AppointmentUseCase implements AppointmentQueryPort  {

    private final AppointmentRepositoryPort appointmentRepositoryPort;
    private final ContractorRepositoryPort contractorRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    private final AppointmentDomainService appointmentDomainService;
    private final AvailabilityRuleRepositoryPort availabilityRuleRepositoryPort ;
    private final UnavailabilityRuleRepositoryPort unavailabilityRuleRepositoryPort;

    public AppointmentUseCase(AppointmentRepositoryPort appointmentRepositoryPort, ContractorRepositoryPort contractorRepositoryPort, ClientRepositoryPort clientRepositoryPort, UserRepositoryPort userRepositoryPort, AppointmentDomainService appointmentDomainService, AvailabilityRuleRepositoryPort availabilityRuleRepositoryPort, UnavailabilityRuleRepositoryPort unavailabilityRuleRepositoryPort) {
        this.appointmentRepositoryPort = appointmentRepositoryPort;
        this.contractorRepositoryPort = contractorRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.appointmentDomainService = appointmentDomainService;
        this.availabilityRuleRepositoryPort = availabilityRuleRepositoryPort;
        this.unavailabilityRuleRepositoryPort = unavailabilityRuleRepositoryPort;
    }


    // ========== BookAppointmentUseCase ==========

    @Override
    public Appointment bookAppointment(Appointment query) {

        if (!contractorRepositoryPort.isActive(query.getContractorId())) {
            throw new ContractorNotActiveException(
                    "Le prestataire n'existe pas ou n'est pas actif"
            );
        }

        if (!userRepositoryPort.isUserActive(query.getClientId())) {
            throw new IllegalArgumentException("Le client n'existe pas");
        }


        if (!isSlotAvailable(query.getContractorId(), query.getSlot().startDateTime(), query.getSlot().duration())) {
            throw new SlotNotAvailableException(
                    String.format("Le créneau du %s à %s n'est pas disponible",
                            query.getSlot().startDateTime().toLocalDate(),
                            query.getSlot().startDateTime().toLocalTime())
            );
        }

        boolean alreadyBooked = appointmentRepositoryPort
                .existsByContractorAndDateTime(query.getContractorId(), query.getSlot().startDateTime());

        if (alreadyBooked) {
            throw new SlotNotAvailableException("Ce créneau est déja réservé !");
        }



        if(!this.appointmentDomainService.
                canCreateAppointment(
                     query.getAppointmentDateTime(),
                     query.getDuration(),
                        this.availabilityRuleRepositoryPort.findActiveByContractorId(query.getContractorId()),
                        this.unavailabilityRuleRepositoryPort.findByContractorIdAndDateRange(
                                    query.getContractorId(),
                                    query.getAppointmentDateTime().toLocalDate(),
                                    query.getEndDateTime().toLocalDate()
                                ), List.of() // flemme de continuer
                )
        ){
            throw new IllegalArgumentException("Vous ne pouvez pas réserver ce rendez-vous");
        }

        Appointment appointment = Appointment.create(
                null,
                query.getClientId(),
                query.getContractorId(),
                query.getSlot().startDateTime(),
                query.getSlot().duration(),
                query.getReason(),
                query.getNotes()
        );

        // 6. Sauvegarder
        return appointmentRepositoryPort.save(appointment);
    }

    // ========== ManageAppointmentUseCase ==========

    @Override
    public void confirmAppointment(UUID appointmentId) {
        Appointment appointment = getAppointmentOrThrow(appointmentId);


        appointment.confirm();
        appointmentRepositoryPort.save(appointment);
    }

    @Override
    public void cancelAppointment(UUID appointmentId, String reason, UUID requesterId) {
        Appointment appointment = getAppointmentOrThrow(appointmentId);

        // Vérifier que le requester est soit le client soit le contractor
        if (!appointment.getClientId().equals(requesterId) &&
                !appointment.getContractorId().equals(requesterId)) {
            throw new UnauthorizedAppointmentActionException(
                    "Vous n'êtes pas autorisé à annuler ce rendez-vous"
            );
        }

        appointment.cancel(reason != null ? reason : "Annulation demandée");
        appointmentRepositoryPort.save(appointment);
    }

    @Override
    public void completeAppointment(UUID appointmentId) {
        Appointment appointment = getAppointmentOrThrow(appointmentId);
        appointment.complete();
        appointmentRepositoryPort.save(appointment);
    }

    @Override
    public void addNoteToAppointment(UUID appointmentId, String note) {
        Appointment appointment = getAppointmentOrThrow(appointmentId);
        appointment.addNote(note);
        appointmentRepositoryPort.save(appointment);
    }

    // ========== ViewAppointmentsUseCase ==========

    @Override
    public List<Appointment> getClientAppointments(UUID clientId) {
        return appointmentRepositoryPort.findByClientId(clientId);
    }

    @Override
    public List<Appointment> getUpcomingClientAppointments(UUID clientId) {
        return appointmentRepositoryPort.findByClientId(clientId).stream()
                .filter(apt -> apt.getSlot().isInFuture())
                .filter(apt -> apt.getStatus() != AppointmentStatus.CANCELLED)
                .sorted((a1, a2) -> a1.getAppointmentDateTime().compareTo(a2.getAppointmentDateTime()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Appointment> getContractorAppointments(UUID contractorId) {
        return appointmentRepositoryPort.findByContractorId(contractorId);
    }

    @Override
    public List<Appointment> getContractorAppointmentsByDate(UUID contractorId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        return appointmentRepositoryPort
                .findByContractorIdAndDateRange(contractorId, startOfDay, endOfDay);
    }

    @Override
    public Appointment getAppointmentById(UUID appointmentId) {
        return getAppointmentOrThrow(appointmentId);
    }

    // ========== CheckAvailabilityUseCase ==========

    @Override
    public boolean isSlotAvailable(UUID contractorId, LocalDateTime startDateTime, int duration) {
        return true;
        //return slotGeneratorService.isSlotAvailable(startDateTime, duration);
    }

    @Override
    public List<TimeSlot> getAvailableSlots(UUID contractorId, LocalDate startDate, LocalDate endDate) {
        return  null;
        //return slotGeneratorService.findOnlyAvailableSlots(contractorId, startDate, endDate);
    }

    @Override
    public List<TimeSlot> getAvailableSlotsForDay(UUID contractorId, LocalDate date) {
        return getAvailableSlots(contractorId, date, date);
    }

    // ========== Méthodes privées ==========

    private Appointment getAppointmentOrThrow(UUID appointmentId) {
        return appointmentRepositoryPort.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Rendez-vous non trouvé avec l'ID: " + appointmentId
                ));
    }
}