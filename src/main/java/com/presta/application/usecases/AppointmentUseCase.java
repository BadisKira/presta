package com.presta.application.usecases;


import com.presta.domain.exception.AppointmentNotFoundException;
import com.presta.domain.exception.ContractorNotActiveException;
import com.presta.domain.exception.SlotNotAvailableException;
import com.presta.domain.exception.UnauthorizedAppointmentActionException;
import com.presta.domain.model.Appointment;
import com.presta.domain.model.valueobject.AppointmentStatus;
import com.presta.domain.port.in.appointment.AppointmentQueryPort;
import com.presta.domain.port.out.ClientRepositoryPort;
import com.presta.domain.port.out.ContractorRepositoryPort;
import com.presta.domain.port.out.*;
import com.presta.domain.service.SlotGeneratorService;
import com.presta.domain.model.valueobject.TimeSlot;

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
    private final SlotGeneratorService slotGeneratorService;
    private final UserRepositoryPort userRepositoryPort;

    public AppointmentUseCase(AppointmentRepositoryPort appointmentRepositoryPort, ContractorRepositoryPort contractorRepositoryPort, ClientRepositoryPort clientRepositoryPort, SlotGeneratorService slotGeneratorService, UserRepositoryPort userRepositoryPort) {
        this.appointmentRepositoryPort = appointmentRepositoryPort;
        this.contractorRepositoryPort = contractorRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.slotGeneratorService = slotGeneratorService;
        this.userRepositoryPort = userRepositoryPort;
    }


    // ========== BookAppointmentUseCase ==========

    @Override
    public Appointment bookAppointment(AppointmentQueryPort.BookAppointmentQuery query) {
        // 1. Vérifier que le contractor existe et est actif
        if (!contractorRepositoryPort.isActive(query.contractorId())) {
            throw new ContractorNotActiveException(
                    "Le prestataire n'existe pas ou n'est pas actif"
            );
        }

        // 2. Vérifier que le client existe
        if (!userRepositoryPort.isUserActive(query.clientId())) {
            throw new IllegalArgumentException("Le client n'existe pas");
        }

        // 3. Vérifier que le créneau est disponible
        if (!isSlotAvailable(query.contractorId(), query.startDateTime(), query.duration())) {
            throw new SlotNotAvailableException(
                    String.format("Le créneau du %s à %s n'est pas disponible",
                            query.startDateTime().toLocalDate(),
                            query.startDateTime().toLocalTime())
            );
        }

        // 4. Vérifier qu'aucun RDV n'existe déjà (double-check pour éviter les doublons)
        boolean alreadyBooked = appointmentRepositoryPort
                .existsByContractorAndDateTime(query.contractorId(), query.startDateTime());

        if (alreadyBooked) {
            throw new SlotNotAvailableException("Ce créneau vient d'être réservé");
        }

        // 5. Créer le rendez-vous
        Appointment appointment = Appointment.create(
                query.clientId(),
                query.contractorId(),
                query.startDateTime(),
                query.duration(),
                query.reason()
        );

        // 6. Sauvegarder
        return appointmentRepositoryPort.save(appointment);
    }

    // ========== ManageAppointmentUseCase ==========

    @Override
    public void confirmAppointment(UUID appointmentId) {
        Appointment appointment = getAppointmentOrThrow(appointmentId);

        // Seul le contractor peut confirmer
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
        return slotGeneratorService.isSlotAvailable(contractorId, startDateTime, duration);
    }

    @Override
    public List<TimeSlot> getAvailableSlots(UUID contractorId, LocalDate startDate, LocalDate endDate) {
        return slotGeneratorService.findOnlyAvailableSlots(contractorId, startDate, endDate);
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