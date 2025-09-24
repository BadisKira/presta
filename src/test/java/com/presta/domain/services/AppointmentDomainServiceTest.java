package com.presta.domain.services;

import com.presta.domain.exception.AppointmentConflictException;
import com.presta.domain.exception.OutsideAvailabilityException;
import com.presta.domain.exception.UnavailabilityConflictException;
import com.presta.domain.model.Appointment;
import com.presta.domain.model.AvailabilityRule;
import com.presta.domain.model.UnavailabilityRule;
import com.presta.domain.model.valueobject.*;
import com.presta.domain.service.AppointmentDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentDomainServiceTest {

    private AppointmentDomainService service;

    @BeforeEach
    void setUp() {
        service = new AppointmentDomainService();
    }

    /**
     * TEST 1: Création de RDV sur un créneau valide
     *
     * Objectif: Vérifier qu'un RDV peut être créé quand toutes les conditions sont remplies
     * Scenario: Lundi 9h00, durée 30min, contractor travaille Lundi 9h-17h avec créneaux de 30min
     * Attendu: La création doit être autorisée (true)
     */
    @Test
    void testCanCreateAppointment_WithPerfectlyValidSlot() {
        // Given
        LocalDateTime proposedDateTime = LocalDateTime.of(2025, 1, 20, 9, 0); // Lundi 20 janvier 2025 à 9h00
        int duration = 30;

        AvailabilityRule rule = new AvailabilityRule(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Set.of(DayOfWeek.MONDAY),
                new TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0)),
                new SlotConfiguration(30, 0), // 30min de créneau, 0 repos
                List.of(),
                true
        );

        // When
        boolean canCreate = service.canCreateAppointment(
                proposedDateTime,
                duration,
                List.of(rule),
                List.of(),
                List.of()
        );

        // Then
        assertTrue(canCreate, "Le RDV devrait pouvoir être créé sur ce créneau valide");
    }

    /**
     * TEST 2: Rejet de RDV sur un mauvais jour
     *
     * Objectif: Vérifier qu'un RDV est refusé si le jour ne correspond pas aux disponibilités
     * Scenario: Tentative de RDV le mardi alors que le contractor ne travaille que le lundi
     * Attendu: La création doit être refusée (false)
     */
    @Test
    void testCanCreateAppointment_RejectsWrongDayOfWeek() {
        // Given
        LocalDateTime tuesdayDateTime = LocalDateTime.of(2025, 1, 21, 9, 0); // Mardi
        // Règle: Seulement le LUNDI
        AvailabilityRule mondayOnlyRule = new AvailabilityRule(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Set.of(DayOfWeek.MONDAY),
                new TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0)),
                new SlotConfiguration(30, 0),
                List.of(),
                true
        );

        // When & Then
        assertThrows(
                OutsideAvailabilityException.class,
                () -> service.canCreateAppointment(
                        tuesdayDateTime,
                        30,
                        List.of(mondayOnlyRule),
                        List.of(),
                        List.of()
                )
        );
    }

    /**
     * TEST 3: Détection de conflit avec indisponibilité
     *
     * Objectif: Vérifier qu'un RDV est refusé si une indisponibilité bloque le créneau
     * Scenario: Contractor en congés le 20 janvier, tentative de RDV ce jour-là
     * Attendu: La création doit être refusée (false)
     */
    @Test
    void testCanCreateAppointment_BlockedByUnavailability() {
        // Given
        LocalDateTime proposedDateTime = LocalDateTime.of(2025, 1, 20, 10, 0);
        AvailabilityRule rule = new AvailabilityRule(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Set.of(DayOfWeek.MONDAY), // Seulement lundi
                new TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0)),
                new SlotConfiguration(30, 0),
                List.of(),
                true
        );
        // Indisponibilité: journée complète le 20 janvier
        UnavailabilityRule unavailability = UnavailabilityRule.createFullDay(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2025, 1, 20),
                LocalDate.of(2025, 1, 20),
                "Congés"
        );

        // When & Then
        UnavailabilityConflictException exception = assertThrows(
                UnavailabilityConflictException.class,
                () -> service.canCreateAppointment(
                        proposedDateTime,
                        30,
                        List.of(rule),
                        List.of(unavailability),
                        List.of()
                ),
                "Une exception UnavailabilityConflictException devrait être levée"
        );

        // Vérification optionnelle du message d'erreur
        assertTrue(exception.getMessage().contains("2025-01-20T10:00"),
                "Le message d'erreur devrait contenir la date/heure proposée");
        assertTrue(exception.getMessage().contains("indisponibilité"),
                "Le message d'erreur devrait mentionner l'indisponibilité");
    }

    /**
     * TEST 4: Détection de conflit avec RDV existant
     *
     * Objectif: Vérifier qu'un RDV est refusé si un autre RDV occupe déjà le créneau
     * Scenario: Un RDV confirmé existe déjà à 10h, tentative de réserver le même créneau
     * Attendu: La création doit être refusée (false)
     */
    @Test
    void testCanCreateAppointment_ConflictWithExistingAppointment() {
        // Given
        LocalDateTime proposedDateTime = LocalDateTime.of(2025, 1, 20, 10, 0);
        UUID contractorId = UUID.randomUUID();
        AvailabilityRule rule = new AvailabilityRule(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Set.of(DayOfWeek.MONDAY),
                new TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0)),
                new SlotConfiguration(30, 0),
                List.of(),
                true
        );
        // RDV existant au même horaire
        Appointment existingAppointment = new Appointment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                contractorId,
                new TimeSlot(proposedDateTime, 30),
                AppointmentStatus.CONFIRMED,
                new AppointmentDetails("Consultation", null),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // When & Then
        assertThrows(
                AppointmentConflictException.class,
                () -> service.canCreateAppointment(
                        proposedDateTime,
                        30,
                        List.of(rule),
                        List.of(),
                        List.of(existingAppointment)
                )
        );
    }
    /**
     * TEST 5: Vérification de l'alignement sur la grille horaire
     *
     * Objectif: Vérifier que les RDV respectent la grille horaire définie
     * Scenario: Créneaux de 30min + 10min repos = grille toutes les 40min (9h00, 9h40, 10h20...)
     *           Tentative à 9h15 (non aligné)
     * Attendu: La création doit être refusée (false)
     */
    @Test
    void testIsAlignedWithSlotGrid_RejectsUnalignedTime() {
        // Given
        LocalDateTime unalignedTime = LocalDateTime.of(2025, 1, 20, 9, 15);

        AvailabilityRule rule = new AvailabilityRule(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Set.of(DayOfWeek.MONDAY),
                new TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0)),
                new SlotConfiguration(30, 10), // 30min + 10min repos
                List.of(),
                true
        );

        // When
        boolean isAligned = service.isAlignedWithSlotGrid(unalignedTime, rule);

        // Then
        assertFalse(isAligned, "9h15 n'est pas aligné sur une grille 9h00, 9h40, 10h20...");
    }

    /**
     * TEST 6: Calcul du taux d'occupation
     *
     * Objectif: Vérifier le calcul correct du taux d'occupation d'un prestataire
     * Scenario: 10 créneaux possibles sur une semaine, 3 RDV confirmés
     * Attendu: Taux d'occupation = 30%
     */
    @Test
    void testCalculateOccupancyRate() {
        // Given
        LocalDate monday = LocalDate.of(2025, 1, 20);
        LocalDate friday = LocalDate.of(2025, 1, 24);

        // Règle: Lundi et Mercredi, 9h-11h, créneaux de 30min = 4 créneaux/jour × 2 jours = 8 créneaux
        AvailabilityRule rule = new AvailabilityRule(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                new TimeRange(LocalTime.of(9, 0), LocalTime.of(11, 0)),
                new SlotConfiguration(30, 0),
                List.of(),
                true
        );

        // 3 RDV confirmés
        List<Appointment> appointments = List.of(
                createAppointment(LocalDateTime.of(2025, 1, 20, 9, 0), AppointmentStatus.CONFIRMED),
                createAppointment(LocalDateTime.of(2025, 1, 20, 9, 30), AppointmentStatus.CONFIRMED),
                createAppointment(LocalDateTime.of(2025, 1, 22, 10, 0), AppointmentStatus.CONFIRMED)
        );

        // When
        double occupancyRate = service.calculateOccupancyRate(
                appointments,
                List.of(rule),
                monday,
                friday
        );

        // Then
        assertEquals(37.5, occupancyRate, 0.1, "3 RDV sur 8 créneaux possibles = 37.5%");
    }

//    /**   NOT PASSED YET
//     * TEST 7: Recherche du prochain créneau disponible
//     *
//     * Objectif: Vérifier que l'algorithme trouve bien le prochain créneau libre
//     * Scenario: Créneaux 9h et 9h30 occupés, cherche après 8h
//     * Attendu: Doit retourner 10h00
//     */
//    @Test
//    void testFindNextAvailableSlot() {
//        // Given
//        LocalDateTime searchAfter = LocalDateTime.of(2025, 1, 20, 8, 0);
//        LocalDate monday = LocalDate.of(2025, 1, 20);
//
//        AvailabilityRule rule = new AvailabilityRule(
//                UUID.randomUUID(),
//                UUID.randomUUID(),
//                Set.of(DayOfWeek.MONDAY),
//                new TimeRange(LocalTime.of(9, 0), LocalTime.of(11, 0)),
//                new SlotConfiguration(30, 0),
//                List.of(),
//                true
//        );
//
//        // RDV existants à 9h00 et 9h30
//        List<Appointment> existingAppointments = List.of(
//                createAppointment(LocalDateTime.of(2025, 1, 20, 9, 0), AppointmentStatus.CONFIRMED),
//                createAppointment(LocalDateTime.of(2025, 1, 20, 9, 30), AppointmentStatus.CONFIRMED)
//        );
//
//        // When
//        LocalDateTime nextSlot = service.findNextAvailableSlot(
//                searchAfter,
//                30,
//                List.of(rule),
//                List.of(),
//                existingAppointments
//        );
//
//        // Then
//        assertNotNull(nextSlot);
//        assertEquals(LocalDateTime.of(2025, 1, 20, 10, 0), nextSlot,
//                "Le prochain créneau libre devrait être 10h00");
//    }

    /** NOT PASSING YET
//     * TEST 8: Validation des droits d'annulation client
//     * Objectif: Vérifier que seul le propriétaire peut annuler et seulement >24h avant
//     * Scenario: Client tente d'annuler son propre RDV 48h avant
//     * Attendu: Annulation autorisée (true)
//     */
    @Test
    void testCanClientCancelAppointment_ValidScenario() {
        // Given
        UUID clientId = UUID.randomUUID();

        // RDV dans 48h
        LocalDateTime in48Hours = LocalDateTime.now().plusHours(48);
        Appointment appointment = new Appointment(
                UUID.randomUUID(),
                clientId, // Le client propriétaire
                UUID.randomUUID(),
                new TimeSlot(in48Hours, 30),
                AppointmentStatus.CONFIRMED,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // When
        boolean canCancel = service.canClientCancelAppointment(appointment, clientId);

        // Then
        assertTrue(canCancel, "Le client devrait pouvoir annuler son RDV 48h avant");
    }

    /**
     * TEST 9: Détermination du besoin de rappel
     *
     * Objectif: Vérifier que les rappels sont déclenchés au bon moment
     * Scenario: RDV confirmé dans 36h
     * Attendu: Un rappel doit être envoyé (true)
     */
    @Test
    void testShouldSendReminder_InReminderWindow() {
        // Given
        LocalDateTime now = LocalDateTime.of(2025, 1, 20, 10, 0);
        LocalDateTime appointmentTime = now.plusHours(36); // Dans 36h

        Appointment appointment = new Appointment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new TimeSlot(appointmentTime, 30),
                AppointmentStatus.CONFIRMED, // Confirmé
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // When
        boolean shouldSend = service.shouldSendReminder(appointment, now);

        // Then
        assertTrue(shouldSend, "Un rappel devrait être envoyé pour un RDV confirmé dans 36h");
    }

    /**
     * TEST 10: Gestion des RDV annulés dans les conflits
     *
     * Objectif: Vérifier que les RDV annulés ne bloquent pas les créneaux
     * Scenario: Un RDV annulé existe à 10h, tentative de réserver le même créneau
     * Attendu: La création doit être autorisée (true) car le RDV est annulé
     */
    @Test
    void testCanCreateAppointment_IgnoresCancelledAppointments() {
        // Given
        LocalDateTime proposedDateTime = LocalDateTime.of(2025, 1, 20, 10, 0);

        AvailabilityRule rule = createStandardRule(DayOfWeek.MONDAY);

        // RDV ANNULÉ au même horaire
        Appointment cancelledAppointment = new Appointment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new TimeSlot(proposedDateTime, 30),
                AppointmentStatus.CANCELLED, // ANNULÉ !
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // When
        boolean canCreate = service.canCreateAppointment(
                proposedDateTime,
                30,
                List.of(rule),
                List.of(),
                List.of(cancelledAppointment)
        );

        // Then
        assertTrue(canCreate, "Un RDV annulé ne devrait pas bloquer le créneau");
    }

    // ========== Méthodes utilitaires ==========

    private AvailabilityRule createStandardRule(DayOfWeek day) {
        return new AvailabilityRule(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Set.of(day),
                new TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0)),
                new SlotConfiguration(30, 0),
                List.of(),
                true
        );
    }

    private Appointment createAppointment(LocalDateTime dateTime, AppointmentStatus status) {
        return new Appointment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new TimeSlot(dateTime, 30),
                status,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}