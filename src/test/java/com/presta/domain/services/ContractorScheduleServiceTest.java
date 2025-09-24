package com.presta.domain.services;

import com.presta.domain.model.Appointment;
import com.presta.domain.model.AvailabilityRule;
import com.presta.domain.model.UnavailabilityRule;
import com.presta.domain.model.valueobject.*;
import com.presta.domain.service.ContractorScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ContractorScheduleServiceTest {

    private ContractorScheduleService service;
    private UUID contractorId;
    private LocalDate today;
    private LocalDate tomorrow;

    @BeforeEach
    void setUp() {
        service = new ContractorScheduleService();
        contractorId = UUID.randomUUID();
        today = LocalDate.now();
        tomorrow = today.plusDays(1);
    }

    @Test
    @DisplayName("Should return empty list when contractorId is null")
    void generateSlots_WithNullContractorId_ReturnsEmptyList() {
        // Given
        AvailabilityRule rule = createBasicAvailabilityRule();

        // When
        List<AvailableSlot> result = service.generateSlots(
                null, rule, List.of(), List.of(), today, tomorrow
        );

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when availabilityRule is null")
    void generateSlots_WithNullAvailabilityRule_ReturnsEmptyList() {
        // When
        List<AvailableSlot> result = service.generateSlots(
                contractorId, null, List.of(), List.of(), today, tomorrow
        );

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when start date is after end date")
    void generateSlots_WithInvalidDateRange_ReturnsEmptyList() {
        // Given
        AvailabilityRule rule = createBasicAvailabilityRule();
        LocalDate startDate = today.plusDays(2);
        LocalDate endDate = today;

        // When
        List<AvailableSlot> result = service.generateSlots(
                contractorId, rule, List.of(), List.of(), startDate, endDate
        );

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when availability rule is inactive")
    void generateSlots_WithInactiveRule_ReturnsEmptyList() {
        // Given
        AvailabilityRule rule = createInactiveAvailabilityRule();

        // When
        List<AvailableSlot> result = service.generateSlots(
                contractorId, rule, List.of(), List.of(), today, tomorrow
        );

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should generate available slots when no constraints")
    void generateSlots_WithNoConstraints_GeneratesAvailableSlots() {
        // Given
        AvailabilityRule rule = createMondayToFridayRule();
        LocalDate monday = getNextMonday();
        LocalDate tuesday = monday.plusDays(1);

        // When
        List<AvailableSlot> result = service.generateSlots(
                contractorId, rule, null, null, monday, tuesday
        );

        // Then
        assertFalse(result.isEmpty());
        // Vérifier que tous les créneaux futurs sont AVAILABLE
        result.stream()
                .filter(slot -> !slot.timeSlot().isInPast())
                .forEach(slot -> assertEquals(AvailabilityStatus.AVAILABLE, slot.status()));
    }

    @Test
    @DisplayName("Should mark past slots as PAST")
    void generateSlots_WithPastSlots_MarksSlotsAsPast() {
        // Given
        AvailabilityRule rule = createBasicAvailabilityRule();
        LocalDate yesterday = today.minusDays(1);

        // When
        List<AvailableSlot> result = service.generateSlots(
                contractorId, rule, List.of(), List.of(), yesterday, yesterday
        );

        // Then
        if (!result.isEmpty()) {
            result.forEach(slot ->
                    assertEquals(AvailabilityStatus.PAST, slot.status())
            );
        }
    }

    @Test
    @DisplayName("Should mark slots as UNAVAILABLE when blocked by UnavailabilityRule")
    void generateSlots_WithUnavailabilityRule_MarksSlotsAsUnavailable() {
        // Given
        AvailabilityRule rule = createMondayToFridayRule();
        LocalDate monday = getNextMonday();

        // Créer une indisponibilité pour lundi matin (9h-12h)
        UnavailabilityRule unavailability = UnavailabilityRule.create(
                UUID.randomUUID(),
                contractorId,
                monday,
                monday,
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                "Congé matin"
        );

        // When
        List<AvailableSlot> result = service.generateSlots(
                contractorId, rule, List.of(unavailability), List.of(), monday, monday
        );


        // Vérifier que les créneaux du matin sont UNAVAILABLE
        long unavailableCount = result.stream()
                .filter(slot -> slot.timeSlot().startDateTime().toLocalTime().isBefore(LocalTime.of(12, 0)))
                .filter(slot -> slot.status() == AvailabilityStatus.UNAVAILABLE)
                .count();

        assertTrue(unavailableCount > 0, "Should have unavailable slots in the morning");

        // Vérifier que les créneaux de l'après-midi sont AVAILABLE (si futurs)
        result.stream()
                .filter(slot -> slot.timeSlot().startDateTime().toLocalTime().isAfter(LocalTime.of(12, 0)))
                .filter(slot -> !slot.timeSlot().isInPast())
                .forEach(slot -> assertEquals(AvailabilityStatus.AVAILABLE, slot.status()));
    }

    @Test
    @DisplayName("Should mark slots as BOOKED when overlapping with confirmed appointment")
    void generateSlots_WithConfirmedAppointment_MarksSlotsAsBooked() {
        // Given
        AvailabilityRule rule = createMondayToFridayRule();
        LocalDate monday = getNextMonday();
        LocalDateTime appointmentTime = monday.atTime(10, 0);

        // Créer un rendez-vous confirmé (sans Mock)
        TimeSlot appointmentSlot = new TimeSlot(appointmentTime, 60);
        Appointment confirmedAppointment = createAppointment(
                appointmentSlot,
                AppointmentStatus.CONFIRMED
        );

        // When
        List<AvailableSlot> result = service.generateSlots(
                contractorId, rule, List.of(), List.of(confirmedAppointment), monday, monday
        );

        // Then
        assertFalse(result.isEmpty());

        // Vérifier qu'il y a au moins un créneau BOOKED à 10h
        boolean hasBookedSlot = result.stream()
                .anyMatch(slot ->
                        slot.timeSlot().startDateTime().equals(appointmentTime) &&
                                slot.status() == AvailabilityStatus.BOOKED
                );

        assertTrue(hasBookedSlot, "Should have a booked slot at appointment time");
    }

    @Test
    @DisplayName("Should not mark slots as BOOKED for cancelled appointments")
    void generateSlots_WithCancelledAppointment_DoesNotMarkAsBooked() {
        // Given
        AvailabilityRule rule = createMondayToFridayRule();
        LocalDate monday = getNextMonday();
        LocalDateTime appointmentTime = monday.atTime(10, 0);

        // Créer un rendez-vous annulé (sans Mock)
        TimeSlot appointmentSlot = new TimeSlot(appointmentTime, 60);
        Appointment cancelledAppointment = createAppointment(
                appointmentSlot,
                AppointmentStatus.CANCELLED
        );

        // When
        List<AvailableSlot> result = service.generateSlots(
                contractorId, rule, List.of(), List.of(cancelledAppointment), monday, monday
        );

        // Then
        // Le créneau ne doit PAS être marqué comme BOOKED
        result.stream()
                .filter(slot -> slot.timeSlot().startDateTime().equals(appointmentTime))
                .filter(slot -> !slot.timeSlot().isInPast())
                .forEach(slot ->
                        assertNotEquals(AvailabilityStatus.BOOKED, slot.status())
                );
    }

    @Test
    @DisplayName("Should respect priority order: PAST > UNAVAILABLE > BOOKED > AVAILABLE")
    void generateSlots_WithMultipleConstraints_RespectsStatusPriority() {
        // Given
        AvailabilityRule rule = createMondayToFridayRule();
        LocalDate pastMonday = today.minusWeeks(1).with(DayOfWeek.MONDAY);

        // Créer une indisponibilité sur un jour passé
        UnavailabilityRule unavailability = UnavailabilityRule.createFullDay(
                UUID.randomUUID(),
                contractorId, pastMonday, pastMonday, "Congé"
        );

        // When
        List<AvailableSlot> result = service.generateSlots(
                contractorId, rule, List.of(unavailability), List.of(), pastMonday, pastMonday
        );

        // Then
        // Tous les créneaux doivent être PAST (pas UNAVAILABLE) car le passé a priorité
        result.forEach(slot ->
                assertEquals(AvailabilityStatus.PAST, slot.status())
        );
    }

    @Test
    @DisplayName("Should handle null lists gracefully")
    void generateSlots_WithNullLists_HandlesGracefully() {
        // Given
        AvailabilityRule rule = createMondayToFridayRule();
        LocalDate monday = getNextMonday();

        // When
        List<AvailableSlot> result = service.generateSlots(
                contractorId, rule, null, null, monday, monday
        );

        // Then
        assertFalse(result.isEmpty());
        // Should not throw NPE and generate slots normally
    }

    @Test
    @DisplayName("Should sort slots chronologically")
    void generateSlots_WithMultipleDays_SortsSlotsChronologically() {
        // Given
        AvailabilityRule rule = createMondayToFridayRule();
        LocalDate monday = getNextMonday();
        LocalDate wednesday = monday.plusDays(2);

        // When
        List<AvailableSlot> result = service.generateSlots(
                contractorId, rule, List.of(), List.of(), monday, wednesday
        );

        // Then
        assertFalse(result.isEmpty());

        // Vérifier que les créneaux sont triés
        for (int i = 1; i < result.size(); i++) {
            LocalDateTime previous = result.get(i-1).timeSlot().startDateTime();
            LocalDateTime current = result.get(i).timeSlot().startDateTime();
            assertTrue(previous.isBefore(current) || previous.equals(current),
                    "Slots should be sorted chronologically");
        }
    }

    // ========== Helper Methods ==========

    private AvailabilityRule createBasicAvailabilityRule() {
        Set<DayOfWeek> allDays = EnumSet.allOf(DayOfWeek.class);
        TimeRange timeRange = new TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0));
        SlotConfiguration slotConfig = new SlotConfiguration(60, 0);

        return new AvailabilityRule(
                UUID.randomUUID(),
                contractorId,
                allDays,
                timeRange,
                slotConfig,
                List.of(),
                true
        );
    }

    private AvailabilityRule createMondayToFridayRule() {
        Set<DayOfWeek> weekDays = EnumSet.of(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        );
        TimeRange timeRange = new TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0));
        SlotConfiguration slotConfig = new SlotConfiguration(60, 0); // Créneaux d'1h sans pause

        return new AvailabilityRule(
                UUID.randomUUID(),
                contractorId,
                weekDays,
                timeRange,
                slotConfig,
                List.of(),
                true
        );
    }

    private AvailabilityRule createInactiveAvailabilityRule() {
        AvailabilityRule rule = createBasicAvailabilityRule();
        // Créer une nouvelle instance inactive
        return new AvailabilityRule(
                rule.getId(),
                rule.getContractorId(),
                rule.getWeekDays(),
                rule.getTimeRange(),
                rule.getSlotConfig(),
                rule.getBreakTimes(),
                false // inactive
        );
    }

    private Appointment createAppointment(TimeSlot slot, AppointmentStatus status) {
        // Créer un vrai objet Appointment pour les tests
        // Adapter selon le constructeur réel de ta classe Appointment
        return new Appointment(
                UUID.randomUUID(),     // id
                UUID.randomUUID(),     // clientId
                contractorId,          // contractorId
                slot,                  // timeSlot
                status,                // status
                new AppointmentDetails("Test appointment",""),    // description
                LocalDateTime.now(),// createdAt
                LocalDateTime.now()// updatedAt
        );
    }

    private LocalDate getNextMonday() {
        return today.getDayOfWeek() == DayOfWeek.MONDAY
                ? today.plusWeeks(1)
                : today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }
}