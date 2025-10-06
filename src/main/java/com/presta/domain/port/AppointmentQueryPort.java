package com.presta.domain.port;

import com.presta.domain.model.Appointment;
import com.presta.domain.model.valueobject.TimeSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.presta.domain.exception.*;
import com.presta.infrastructure.web.dtos.appointment.CreateAppointmentRequest;

public interface AppointmentQueryPort {
//    /**
//     * Commande pour réserver un rendez-vous
//     */
//    record BookAppointmentQuery(
//            UUID clientId,
//            UUID contractorId,
//            LocalDateTime startDateTime,
//            int duration,
//            String reason
//    ) {
//        public BookAppointmentQuery {
//            if (clientId == null || contractorId == null || startDateTime == null) {
//                throw new IllegalArgumentException("Tous les champs obligatoires doivent être renseignés");
//            }
//            if (duration <= 0 || duration > 480) {
//                throw new IllegalArgumentException("La durée doit être entre 1 et 480 minutes");
//            }
//            if (startDateTime.isBefore(LocalDateTime.now())) {
//                throw new IllegalArgumentException("Impossible de réserver dans le passé");
//            }
//        }
//    }

    /**
     * Réserve un nouveau rendez-vous
     * @throws SlotNotAvailableException si le créneau n'est pas disponible
     * @throws ContractorNotActiveException si le prestataire n'est pas actif
     */
    Appointment bookAppointment(Appointment command);


    /**
     * Confirme un rendez-vous (action du prestataire)
     */ 
    void confirmAppointment(UUID appointmentId);

    /**
     * Annule un rendez-vous
     * @param appointmentId ID du rendez-vous
     * @param reason Raison de l'annulation
     * @param requesterId ID de celui qui annule (client ou contractor)
     */
    void cancelAppointment(UUID appointmentId, String reason, UUID requesterId);

    /**
     * Marque un rendez-vous comme terminé
     */
    void completeAppointment(UUID appointmentId);

    /**
     * Ajoute une note à un rendez-vous
     */
    void addNoteToAppointment(UUID appointmentId, String note);


    /**
     * Vérifie si un créneau spécifique est disponible
     */
    boolean isSlotAvailable(UUID contractorId, LocalDateTime startDateTime, int duration);

    /**
     * Récupère tous les créneaux disponibles d'un prestataire sur une période
     */
    List<TimeSlot> getAvailableSlots(UUID contractorId, LocalDate startDate, LocalDate endDate);

    /**
     * Récupère les créneaux disponibles d'un prestataire pour un jour donné
     */
    List<TimeSlot> getAvailableSlotsForDay(UUID contractorId, LocalDate date);

    List<Appointment> getClientAppointments(UUID clientId) ;
    List<Appointment> getUpcomingClientAppointments(UUID clientId) ;
    List<Appointment> getContractorAppointments(UUID contractorId);
    List<Appointment> getContractorAppointmentsByDate(UUID contractorId, LocalDate date) ;
    Appointment getAppointmentById(UUID appointmentId) ;
}
