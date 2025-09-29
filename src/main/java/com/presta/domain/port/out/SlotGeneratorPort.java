package com.presta.domain.port.out;

import com.presta.domain.model.Appointment;
import com.presta.domain.model.AvailabilityRule;
import com.presta.domain.model.UnavailabilityRule;
import com.presta.domain.model.valueobject.AvailabilityStatus;
import com.presta.domain.model.valueobject.AvailableSlot;
import com.presta.domain.model.valueobject.TimeSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie pour la génération et gestion des créneaux
 * Interface définissant toutes les opérations métier sur les créneaux
 */
public interface SlotGeneratorPort {

    // ========== GÉNÉRATION DE CRÉNEAUX ==========

    /**
     * Génère tous les créneaux bruts pour une période depuis les règles de disponibilité
     * Note: Les BreakTime sont déjà appliqués via rule.generateSlotsForDay()
     *
     * @param availabilityRules Règles de disponibilité actives du contractor
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Liste des créneaux générés (non filtrés)
     */
    List<TimeSlot> generateRawSlots(
            List<AvailabilityRule> availabilityRules,
            LocalDate startDate,
            LocalDate endDate
    );

    // ========== MARQUAGE ET FILTRAGE ==========

    /**
     * Marque chaque créneau avec son statut de disponibilité
     * Applique les contraintes (UnavailabilityRule et Appointments)
     *
     * @param contractorId ID du contractor
     * @param rawSlots Créneaux bruts à évaluer
     * @param unavailabilityRules Règles d'indisponibilité (vacances, absences)
     * @param existingAppointments Rendez-vous déjà réservés
     * @return Liste des créneaux avec leur statut
     */
    List<AvailableSlot> markSlotsAvailability(
            UUID contractorId,
            List<TimeSlot> rawSlots,
            List<UnavailabilityRule> unavailabilityRules,
            List<Appointment> existingAppointments
    );

    /**
     * Détermine le statut d'un créneau spécifique
     *
     * @param slot Créneau à évaluer
     * @param unavailabilityRules Règles d'indisponibilité
     * @param appointments Rendez-vous existants
     * @return Statut du créneau (AVAILABLE, BOOKED, UNAVAILABLE, PAST)
     */
    AvailabilityStatus determineSlotStatus(
            TimeSlot slot,
            List<UnavailabilityRule> unavailabilityRules,
            List<Appointment> appointments
    );

    // ========== EXTRACTION ET RECHERCHE ==========

    /**
     * Extrait uniquement les créneaux disponibles (status = AVAILABLE)
     *
     * @param markedSlots Créneaux avec leur statut
     * @return Liste des créneaux disponibles uniquement
     */
    List<TimeSlot> extractAvailableSlots(List<AvailableSlot> markedSlots);

    /**
     * Filtre les créneaux selon un statut spécifique
     *
     * @param markedSlots Créneaux avec leur statut
     * @param status Statut recherché
     * @return Créneaux correspondant au statut
     */
    List<TimeSlot> filterSlotsByStatus(
            List<AvailableSlot> markedSlots,
            AvailabilityStatus status
    );

    /**
     * Trouve le prochain créneau disponible après une date/heure donnée
     *
     * @param availableSlots Liste des créneaux disponibles
     * @param afterDateTime À partir de quand chercher
     * @param requiredDuration Durée minimale requise en minutes
     * @return Prochain créneau disponible ou empty
     */
    Optional<TimeSlot> findNextAvailableSlot(
            List<AvailableSlot> availableSlots,
            LocalDateTime afterDateTime,
            int requiredDuration
    );

    /**
     * Trouve tous les créneaux disponibles d'une durée minimale
     *
     * @param availableSlots Liste des créneaux
     * @param minDuration Durée minimale en minutes
     * @return Créneaux de durée suffisante
     */
    List<TimeSlot> findSlotsByMinDuration(
            List<AvailableSlot> availableSlots,
            int minDuration
    );

    // ========== VALIDATION ==========

    /**
     * Vérifie si un créneau spécifique est disponible
     *
     * @param requestedSlot Créneau demandé
     * @param availableSlots Liste des créneaux disponibles
     * @return true si le créneau est disponible
     */
    boolean isSpecificSlotAvailable(
            TimeSlot requestedSlot,
            List<AvailableSlot> availableSlots
    );

    /**
     * Vérifie si un créneau est réservable (validation métier)
     *
     * @param slot Créneau à valider
     * @return true si le créneau respecte les règles métier
     */
    boolean isSlotBookable(TimeSlot slot);

    /**
     * Vérifie si un rendez-vous bloque un créneau
     *
     * @param appointment Rendez-vous à vérifier
     * @param slot Créneau à vérifier
     * @return true si le rendez-vous bloque le créneau
     */
    boolean isAppointmentBlocking(Appointment appointment, TimeSlot slot);

    /**
     * Vérifie si une règle d'indisponibilité bloque un créneau
     *
     * @param unavailabilityRule Règle à vérifier
     * @param slot Créneau à vérifier
     * @return true si la règle bloque le créneau
     */
    boolean isUnavailabilityBlocking(UnavailabilityRule unavailabilityRule, TimeSlot slot);

    // ========== UTILITAIRES ==========

    /**
     * Groupe les créneaux par jour
     *
     * @param slots Liste des créneaux
     * @return Map des créneaux groupés par date
     */
    Map<LocalDate, List<TimeSlot>> groupSlotsByDay(List<TimeSlot> slots);

    /**
     * Trie les créneaux par ordre chronologique
     *
     * @param slots Liste des créneaux à trier
     * @return Liste triée
     */
    List<TimeSlot> sortSlotsByDateTime(List<TimeSlot> slots);

    /**
     * Calcule les statistiques de disponibilité
     *
     * @param availableSlots Liste des créneaux avec statut
     * @return Statistiques de disponibilité
     */
    SlotStatistics calculateStatistics(List<AvailableSlot> availableSlots);

    /**
     * Fusionne les créneaux contigus (optionnel)
     *
     * @param slots Liste des créneaux
     * @return Liste avec créneaux fusionnés
     */
    List<TimeSlot> mergeContiguousSlots(List<TimeSlot> slots);

    // ========== CLASSE INTERNE POUR STATISTIQUES ==========

    /**
     * Record pour les statistiques de créneaux
     */
    record SlotStatistics(
            int totalSlots,
            int availableSlots,
            int bookedSlots,
            int unavailableSlots,
            int pastSlots,
            double availabilityRate,
            double bookingRate
    ) {}
}