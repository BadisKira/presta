package com.presta.domain.port;

import com.presta.domain.model.Appointment;
import com.presta.domain.model.AvailabilityRule;
import com.presta.domain.model.ContractorPlanning;
import com.presta.domain.model.UnavailabilityRule;
import com.presta.domain.model.valueobject.AvailabilityStatus;
import com.presta.domain.model.valueobject.AvailableSlot;
import com.presta.domain.model.valueobject.TimeSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port définissant toutes les opérations métier pour la gestion
 * du planning d'un contractor
 */
public interface ContractorSchedulePort {

    // ========== GÉNÉRATION DU PLANNING ==========

    /**
     * Génère le planning complet d'un contractor pour une période donnée
     * Prend en compte : AvailabilityRule (avec BreakTimes), UnavailabilityRules et Appointments
     *
     * @param contractorId         ID du contractor
     * @param availabilityRule     Règle de disponibilité active du contractor (avec ses BreakTimes)
     * @param unavailabilityRules  Liste des indisponibilités (vacances, absences) à partir d'aujourd'hui
     * @param existingAppointments Rendez-vous déjà pris sur la période
     * @param startDate            Date de début du planning
     * @param endDate              Date de fin du planning
     * @return Planning complet avec tous les créneaux et leur statut
     */
    ContractorPlanning generatePlanning(
            UUID contractorId,
            AvailabilityRule availabilityRule,
            List<UnavailabilityRule> unavailabilityRules,
            List<Appointment> existingAppointments,
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * Génère uniquement la liste des créneaux avec leur statut
     * Version simplifiée sans wrapper object
     *
     * @param contractorId        ID du contractor
     * @param availabilityRule    Règle de disponibilité avec BreakTimes
     * @param unavailabilityRules Indisponibilités futures
     * @param appointments        Rendez-vous existants
     * @param startDate           Date de début
     * @param endDate             Date de fin
     * @return Liste des créneaux avec leur statut de disponibilité
     */
    List<AvailableSlot> generateSlots(
            UUID contractorId,
            AvailabilityRule availabilityRule,
            List<UnavailabilityRule> unavailabilityRules,
            List<Appointment> appointments,
            LocalDate startDate,
            LocalDate endDate
    );

    // ========== RECHERCHE DE CRÉNEAUX SPÉCIFIQUES ==========

    /**
     * Trouve le prochain créneau disponible le plus proche
     *
     * @param availabilityRule    Règle de disponibilité
     * @param unavailabilityRules Indisponibilités
     * @param appointments        Rendez-vous existants
     * @param fromDateTime        À partir de quelle date/heure chercher
     * @param requiredDuration    Durée requise en minutes
     * @param maxDaysToSearch     Nombre maximum de jours à rechercher (ex: 30)
     * @return Le prochain créneau disponible ou empty si aucun trouvé
     */
    Optional<TimeSlot> findNextAvailableSlot(
            AvailabilityRule availabilityRule,
            List<UnavailabilityRule> unavailabilityRules,
            List<Appointment> appointments,
            LocalDateTime fromDateTime,
            int requiredDuration,
            int maxDaysToSearch
    );

    /**
     * Trouve tous les créneaux disponibles pour une durée spécifique
     *
     * @param contractorId        ID du contractor
     * @param availabilityRule    Règle de disponibilité
     * @param unavailabilityRules Indisponibilités
     * @param appointments        Rendez-vous existants
     * @param startDate           Date de début de recherche
     * @param endDate             Date de fin de recherche
     * @param requiredDuration    Durée exacte recherchée en minutes
     * @return Liste des créneaux disponibles de la durée demandée
     */
    List<TimeSlot> findAvailableSlotsWithDuration(
            UUID contractorId,
            AvailabilityRule availabilityRule,
            List<UnavailabilityRule> unavailabilityRules,
            List<Appointment> appointments,
            LocalDate startDate,
            LocalDate endDate,
            int requiredDuration
    );

    // ========== EXTRACTION PAR STATUT ==========

    /**
     * Récupère tous les créneaux disponibles (libres pour réservation)
     *
     * @param slots Liste des créneaux avec leur statut
     * @return Uniquement les créneaux avec statut AVAILABLE
     */
    List<TimeSlot> extractAvailableSlots(List<AvailableSlot> slots);

    /**
     * Récupère tous les créneaux occupés (déjà réservés)
     *
     * @param slots Liste des créneaux avec leur statut
     * @return Uniquement les créneaux avec statut BOOKED
     */
    List<TimeSlot> extractBookedSlots(List<AvailableSlot> slots);

    /**
     * Récupère tous les créneaux indisponibles (vacances, absences)
     *
     * @param slots Liste des créneaux avec leur statut
     * @return Uniquement les créneaux avec statut UNAVAILABLE
     */
    List<TimeSlot> extractUnavailableSlots(List<AvailableSlot> slots);

    /**
     * Filtre les créneaux par statut
     *
     * @param slots  Liste des créneaux
     * @param status Statut recherché
     * @return Créneaux correspondant au statut
     */
    List<TimeSlot> filterByStatus(List<AvailableSlot> slots, AvailabilityStatus status);

    // ========== VALIDATION ==========

    /**
     * Vérifie si un créneau spécifique est disponible pour réservation
     *
     * @param availabilityRule    Règle de disponibilité
     * @param unavailabilityRules Indisponibilités
     * @param appointments        Rendez-vous existants
     * @param requestedSlot       Créneau demandé
     * @return true si le créneau est disponible et réservable
     */
    boolean isSlotAvailable(
            AvailabilityRule availabilityRule,
            List<UnavailabilityRule> unavailabilityRules,
            List<Appointment> appointments,
            TimeSlot requestedSlot
    );

    /**
     * Vérifie si un contractor a au moins un créneau disponible sur une période
     *
     * @param availabilityRule    Règle de disponibilité
     * @param unavailabilityRules Indisponibilités
     * @param appointments        Rendez-vous existants
     * @param startDate           Date de début
     * @param endDate             Date de fin
     * @return true si au moins un créneau est disponible
     */
    boolean hasAvailability(
            AvailabilityRule availabilityRule,
            List<UnavailabilityRule> unavailabilityRules,
            List<Appointment> appointments,
            LocalDate startDate,
            LocalDate endDate
    );


}
// ========== ANALYSE ET STATISTIQUES (OPTIONNEL) ==========

// /**
//  * Calcule les statistiques de disponibilité pour une période
//  *
//  * @param slots Liste des créneaux avec leur statut
//  * @return Statistiques détaillées
//  */
// ScheduleStatistics calculateStatistics(List<AvailableSlot> slots);

// /**
//  * Groupe les créneaux par jour
//  *
//  * @param slots Liste des créneaux
//  * @return Map avec les créneaux groupés par date
//  */
// Map<LocalDate, List<TimeSlot>> groupSlotsByDay(List<TimeSlot> slots);

// /**
//  * Calcule le taux d'occupation pour une période
//  *
//  * @param slots Liste des créneaux avec leur statut
//  * @return Taux d'occupation en pourcentage (0-100)
//  */
// double calculateOccupancyRate(List<AvailableSlot> slots);

