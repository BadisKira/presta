package com.presta.domain;

import com.presta.domain.model.Appointment;
import com.presta.domain.model.valueobject.AppointmentDetails;
import com.presta.domain.model.valueobject.AppointmentStatus;
import com.presta.domain.model.valueobject.TimeSlot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentTest {

    private UUID clientId;
    private UUID contractorId;
    private UUID appointmentId;
    private LocalDateTime futureDateTime;
    private LocalDateTime pastDateTime;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        contractorId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();
        futureDateTime = LocalDateTime.now().plusDays(2); // RDV dans 2 jours
        pastDateTime = LocalDateTime.now().minusDays(1); // RDV hier
    }

    @Test
    @DisplayName("Test 1: Création d'un RDV valide et vérification des valeurs par défaut")
    void testCreateValidAppointment() {
        // Given
        String reason = "Consultation médicale";
        int duration = 60;

        // When
        Appointment appointment = Appointment.create(
                appointmentId,
                clientId,
                contractorId,
                futureDateTime,
                duration,
                reason,
                "Notes"
        );

        // Then
        assertNotNull(appointment);
        assertEquals(appointmentId, appointment.getId());
        assertEquals(clientId, appointment.getClientId());
        assertEquals(contractorId, appointment.getContractorId());
        assertEquals(futureDateTime, appointment.getAppointmentDateTime());
        assertEquals(duration, appointment.getDuration());
        assertEquals(reason, appointment.getReason());
        assertEquals(AppointmentStatus.PENDING, appointment.getStatus());
        assertNotNull(appointment.getCreatedAt());
        assertNotNull(appointment.getUpdatedAt());
    }

    @Test
    @DisplayName("Test 2: Cycle de vie complet d'un RDV (PENDING -> CONFIRMED -> COMPLETED)")
    void testAppointmentLifecycleComplete() {
        // Given - Création d'un RDV dans le futur
        Appointment appointment = Appointment.create(
                appointmentId,
                clientId,
                contractorId,
                futureDateTime,
                60,
                "Consultation",
                "Notes"
        );

        // When/Then - Confirmation du RDV
        assertEquals(AppointmentStatus.PENDING, appointment.getStatus());
        appointment.confirm();
        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());

        // Simulation du passage du temps - reconstitution avec date passée
        Appointment pastAppointment = new Appointment(
                appointment.getId(),
                appointment.getClientId(),
                appointment.getContractorId(),
                new TimeSlot(pastDateTime, 60),
                AppointmentStatus.CONFIRMED,
                appointment.getDetails(),
                appointment.getCreatedAt(),
                LocalDateTime.now()
        );

        // When/Then - Complétion du RDV
        pastAppointment.complete();
        assertEquals(AppointmentStatus.COMPLETED, pastAppointment.getStatus());

        // Vérification qu'on ne peut plus modifier un RDV complété
        assertThrows(IllegalStateException.class, () -> pastAppointment.confirm());
        assertThrows(IllegalStateException.class, () -> pastAppointment.cancel("Raison"));
    }

    @Test
    @DisplayName("Test 3: Annulation d'un RDV avec validation des règles métier (24h minimum)")
    void testCancelAppointmentBusinessRules() {
        // Test 1: Annulation valide (plus de 24h avant)
        Appointment appointment = Appointment.create(
                appointmentId,
                clientId,
                contractorId,
                LocalDateTime.now().plusDays(2), // Dans 2 jours
                60,
                "Consultation",
                "Notes de la consultation"
        );

        String cancellationReason = "Client indisponible";
        appointment.cancel(cancellationReason);

        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        assertTrue(appointment.getNotes().contains("Annulation: " + cancellationReason));

        // Test 2: Tentative d'annulation moins de 24h avant (devrait échouer)
        Appointment lastMinuteAppointment = Appointment.create(
                UUID.randomUUID(),
                clientId,
                contractorId,
                LocalDateTime.now().plusHours(12), // Dans 12h seulement
                60,
                "Consultation urgente","Naaah pas si urgent que ça"
        );

        assertThrows(IllegalStateException.class,
                () -> lastMinuteAppointment.cancel("Trop tard"),
                "Devrait empêcher l'annulation à moins de 24h"
        );

        // Test 3: Tentative d'annulation d'un RDV déjà annulé
        assertThrows(IllegalStateException.class,
                () -> appointment.cancel("Double annulation"),
                "Ne devrait pas permettre d'annuler un RDV déjà annulé"
        );
    }

//    @Test
//    @DisplayName("Test 4: Validation des données obligatoires à la création")
//    void testAppointmentValidation() {
//        // Test avec ID null
//        assertThrows(IllegalArgumentException.class, () ->
//                        new Appointment(
//                                null, // ID null
//                                clientId,
//                                contractorId,
//                                new TimeSlot(futureDateTime, 60),
//                                AppointmentStatus.PENDING,
//                                new AppointmentDetails("Raison", null),
//                                LocalDateTime.now(),
//                                LocalDateTime.now()
//                        ),
//                "Devrait rejeter un ID null"
//        );
//
//        // Test avec clientId null
//        assertThrows(IllegalArgumentException.class, () ->
//                        new Appointment(
//                                appointmentId,
//                                null, // clientId null
//                                contractorId,
//                                new TimeSlot(futureDateTime, 60),
//                                AppointmentStatus.PENDING,
//                                new AppointmentDetails("Raison", null),
//                                LocalDateTime.now(),
//                                LocalDateTime.now()
//                        ),
//                "Devrait rejeter un clientId null"
//        );
//
//        // Test avec contractorId null
//        assertThrows(IllegalArgumentException.class, () ->
//                        new Appointment(
//                                appointmentId,
//                                clientId,
//                                null, // contractorId null
//                                new TimeSlot(futureDateTime, 60),
//                                AppointmentStatus.PENDING,
//                                new AppointmentDetails("Raison", null),
//                                LocalDateTime.now(),
//                                LocalDateTime.now()
//                        ),
//                "Devrait rejeter un contractorId null"
//        );
//
//        // Test avec TimeSlot null
//        assertThrows(IllegalArgumentException.class, () ->
//                        new Appointment(
//                                appointmentId,
//                                clientId,
//                                contractorId,
//                                null, // TimeSlot null
//                                AppointmentStatus.PENDING,
//                                new AppointmentDetails("Raison", null),
//                                LocalDateTime.now(),
//                                LocalDateTime.now()
//                        ),
//                "Devrait rejeter un TimeSlot null"
//        );
//    }

    @Test
    @DisplayName("Test 5: Vérification du chevauchement de créneaux et modifications")
    void testTimeSlotOverlapAndModifiability() {
        // Création d'un RDV confirmé de 10h à 11h dans 2 jours
        LocalDateTime startTime = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);
        Appointment appointment = Appointment.create(
                appointmentId,
                clientId,
                contractorId,
                startTime,
                60, // 1 heure
                "Consultation",
                "Notes"
        );
        appointment.confirm();

        // Test 1: Vérification du chevauchement avec différents créneaux

        // Chevauchement total (même créneau)
        assertTrue(appointment.blocksTimeSlot(startTime, 60),
                "Devrait bloquer exactement le même créneau");

        // Chevauchement partiel (début pendant le RDV)
        assertTrue(appointment.blocksTimeSlot(startTime.plusMinutes(30), 60),
                "Devrait bloquer un créneau qui commence pendant le RDV");

        // Chevauchement partiel (fin pendant le RDV)
        assertTrue(appointment.blocksTimeSlot(startTime.minusMinutes(30), 60),
                "Devrait bloquer un créneau qui se termine pendant le RDV");

        // Pas de chevauchement (après)
        assertFalse(appointment.blocksTimeSlot(startTime.plusHours(2), 60),
                "Ne devrait pas bloquer un créneau 2h après");

        // Pas de chevauchement (avant)
        assertFalse(appointment.blocksTimeSlot(startTime.minusHours(2), 60),
                "Ne devrait pas bloquer un créneau 2h avant");

        // Test 2: Vérification de la modifiabilité
        assertTrue(appointment.isModifiable(),
                "Un RDV confirmé dans le futur (>24h) devrait être modifiable");

        // Test avec un RDV dans moins de 24h
        Appointment soonAppointment = Appointment.create(
                UUID.randomUUID(),
                clientId,
                contractorId,
                LocalDateTime.now().plusHours(12),
                60,
                "RDV urgent",
                "Urgente"
        );
        soonAppointment.confirm();
        assertFalse(soonAppointment.isModifiable(),
                "Un RDV dans moins de 24h ne devrait pas être modifiable");

        // Test avec un RDV annulé
        Appointment cancelledAppointment = Appointment.create(
                UUID.randomUUID(),
                clientId,
                contractorId,
                LocalDateTime.now().plusDays(3),
                60,
                "RDV à annuler",
                "Naruto "
        );
        cancelledAppointment.cancel("Test");
        assertFalse(cancelledAppointment.isModifiable(),
                "Un RDV annulé ne devrait pas être modifiable");

        // Test 3: Vérification de la méthode isAt
        assertTrue(appointment.isAt(contractorId, startTime),
                "Devrait identifier correctement le RDV du prestataire à cette heure");

        assertFalse(appointment.isAt(UUID.randomUUID(), startTime),
                "Ne devrait pas matcher avec un autre prestataire");

        assertFalse(appointment.isAt(contractorId, startTime.plusHours(1)),
                "Ne devrait pas matcher avec une autre heure");
    }
}