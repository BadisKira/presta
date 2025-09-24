package com.presta.domain.service;

import com.presta.domain.model.Appointment;
import com.presta.domain.model.AvailabilityRule;
import com.presta.domain.model.UnavailabilityRule;
import com.presta.domain.model.valueobject.AppointmentStatus;
import com.presta.domain.model.valueobject.AvailabilityStatus;
import com.presta.domain.model.valueobject.AvailableSlot;
import com.presta.domain.model.valueobject.TimeSlot;
import com.presta.domain.port.in.availability.AppointmentQueryPort;
import com.presta.domain.port.in.availability.AvailabilityQueryPort;
import com.presta.domain.port.in.availability.UnavailabilityQueryPort;
import com.presta.domain.port.in.contractor.ContractorQueryPort;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Domain Service - Générateur de créneaux disponibles
 *
 * Responsabilités :
 * 1. Vérifier que le contractor est actif
 * 2. Générer les créneaux depuis les AvailabilityRule (avec BreakTime)
 * 3. Filtrer les UnavailabilityRule (congés, absences)
 * 4. Filtrer les Appointments existants
 * 5. Filtrer les créneaux dans le passé
 */
public class SlotGeneratorService {

    private final AvailabilityQueryPort availabilityQueryPort;
    private final UnavailabilityQueryPort unavailabilityQueryPort;
    private final AppointmentQueryPort appointmentQueryPort;
    private final ContractorQueryPort contractorQueryPort;

    public SlotGeneratorService(AvailabilityQueryPort availabilityQueryPort, UnavailabilityQueryPort unavailabilityQueryPort, AppointmentQueryPort appointmentQueryPort, ContractorQueryPort contractorQueryPort) {
        this.availabilityQueryPort = availabilityQueryPort;
        this.unavailabilityQueryPort = unavailabilityQueryPort;
        this.appointmentQueryPort = appointmentQueryPort;
        this.contractorQueryPort = contractorQueryPort;
    }


    /**
     * Génère tous les créneaux disponibles pour un prestataire sur une période
     *
     * @param contractorId ID du prestataire
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Liste des créneaux disponibles
     */
    public List<AvailableSlot> generateAvailableSlots(
            UUID contractorId,
            LocalDate startDate,
            LocalDate endDate) {

        // 1. Vérifier que le contractor est actif
        if (!isContractorActive(contractorId)) {
            return Collections.emptyList();
        }

        // 2. Récupérer toutes les règles actives du prestataire
        List<AvailabilityRule> activeRules = availabilityQueryPort
                .findActiveByContractorId(contractorId);

        if (activeRules.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. Générer les créneaux bruts pour chaque jour
        List<TimeSlot> rawSlots = generateRawSlots(activeRules, startDate, endDate);

        // 4. Récupérer les indisponibilités sur la période
        List<UnavailabilityRule> unavailabilities = unavailabilityQueryPort
                .findByContractorIdAndPeriod(contractorId, startDate, endDate);

        // 5. Récupérer les rendez-vous existants sur la période
        List<Appointment> existingAppointments = appointmentQueryPort
                .findActiveByContractorIdAndPeriod(contractorId,
                        startDate.atStartOfDay(),
                        endDate.plusDays(1).atStartOfDay());

        // 6. Filtrer et marquer les créneaux
        return markSlotsAvailability(
                contractorId,
                rawSlots,
                unavailabilities,
                existingAppointments
        );
    }

    /**
     * Génère les créneaux bruts depuis les règles de disponibilité
     */
    private List<TimeSlot> generateRawSlots(
            List<AvailabilityRule> rules,
            LocalDate startDate,
            LocalDate endDate) {

        List<TimeSlot> allSlots = new ArrayList<>();

        // Pour chaque jour de la période
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            for (AvailabilityRule rule : rules) {
                // Utilise ta méthode generateSlotsForDay !
                List<TimeSlot> dailySlots = rule.generateSlotsForDay(currentDate);
                allSlots.addAll(dailySlots);
            }
            currentDate = currentDate.plusDays(1);
        }

        // Trier les créneaux par ordre chronologique
        allSlots.sort(Comparator.comparing(TimeSlot::startDateTime));

        return allSlots;
    }

    /**
     * Marque la disponibilité de chaque créneau
     */
    private List<AvailableSlot> markSlotsAvailability(
            UUID contractorId,
            List<TimeSlot> rawSlots,
            List<UnavailabilityRule> unavailabilities,
            List<Appointment> appointments) {

        List<AvailableSlot> markedSlots = new ArrayList<>();

        for (TimeSlot slot : rawSlots) {
            AvailabilityStatus status = determineSlotStatus(
                    slot,
                    unavailabilities,
                    appointments
            );

            markedSlots.add(new AvailableSlot(
                    contractorId,
                    slot,
                    status
            ));
        }

        return markedSlots;
    }

    /**
     * Détermine le statut d'un créneau
     */
    private AvailabilityStatus determineSlotStatus(
            TimeSlot slot,
            List<UnavailabilityRule> unavailabilities,
            List<Appointment> appointments) {

        // 1. Vérifier si le créneau est dans le passé
        if (slot.isInPast()) {
            return AvailabilityStatus.PAST;
        }

        // 2. Vérifier les indisponibilités
        for (UnavailabilityRule unavailability : unavailabilities) {
            if (unavailability.blocksTimeSlot(slot)) {
                return AvailabilityStatus.UNAVAILABLE;
            }
        }

        // 3. Vérifier les rendez-vous existants
        for (Appointment appointment : appointments) {
            if (isAppointmentBlockingSlot(appointment, slot)) {
                return AvailabilityStatus.BOOKED;
            }
        }

        // 4. Le créneau est disponible !
        return AvailabilityStatus.AVAILABLE;
    }

    /**
     * Vérifie si un rendez-vous bloque un créneau
     */
    private boolean isAppointmentBlockingSlot(Appointment appointment, TimeSlot slot) {
        // Un RDV bloque s'il est actif (PENDING ou CONFIRMED) et qu'il chevauche
        if (appointment.getStatus() == AppointmentStatus.CANCELLED ||
                appointment.getStatus() == AppointmentStatus.COMPLETED) {
            return false;
        }

        return appointment.getSlot().overlaps(slot);
    }

    /**
     * Vérifie si le contractor est actif
     */
    private boolean isContractorActive(UUID contractorId) {
        return contractorQueryPort.isActive(contractorId);
    }

    /**
     * Trouve les créneaux disponibles uniquement
     */
    public List<TimeSlot> findOnlyAvailableSlots(
            UUID contractorId,
            LocalDate startDate,
            LocalDate endDate) {

        return generateAvailableSlots(contractorId, startDate, endDate)
                .stream()
                .filter(slot -> slot.status() == AvailabilityStatus.AVAILABLE)
                .map(AvailableSlot::timeSlot)
                .collect(Collectors.toList());
    }

    /**
     * Vérifie si un créneau spécifique est disponible
     */
    public boolean isSlotAvailable(
            UUID contractorId,
            LocalDateTime startDateTime,
            int duration) {

        TimeSlot requestedSlot = new TimeSlot(startDateTime, duration);

        // Générer les créneaux du jour
        List<AvailableSlot> daySlots = generateAvailableSlots(
                contractorId,
                startDateTime.toLocalDate(),
                startDateTime.toLocalDate()
        );

        // Vérifier si le créneau demandé existe et est disponible
        return daySlots.stream()
                .anyMatch(slot ->
                        slot.timeSlot().equals(requestedSlot) &&
                                slot.status() == AvailabilityStatus.AVAILABLE
                );
    }
}

