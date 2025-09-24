package com.presta.domain;

import com.presta.domain.model.AvailabilityRule;
import com.presta.domain.model.BreakTime;
import com.presta.domain.model.valueobject.SlotConfiguration;
import com.presta.domain.model.valueobject.TimeRange;
import com.presta.domain.model.valueobject.TimeSlot;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@ActiveProfiles("test")
public class AvailabilityRuleTest {

    UUID contractorId = UUID.randomUUID();
    Set<DayOfWeek> weekDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
    TimeRange range = new TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0));
    SlotConfiguration slotConfig = new SlotConfiguration(30,10);
    UUID activeContractorID = UUID.randomUUID();





    @Test
    void SHOULD_NOT_CREATE_AVAILABILITY_RULE() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            AvailabilityRule.create(null, weekDays, range, slotConfig);
        });

        assertEquals("L'ID du prestataire est obligatoire", exception.getMessage());
    }

    @Test
    void SHOULD_CREATE_AVAILABILITY_RULE() {
        // GIVEN
        AvailabilityRule availabilityRule = null;
        // WHEN
        availabilityRule = AvailabilityRule.create(
                activeContractorID,
                weekDays,
                range,
                slotConfig);
        // THEN
        assertEquals(availabilityRule.getContractorId(),activeContractorID);
    }



    @Test
    void SHOULD_GENERATE_A_DAY_PLANNING_FOR_CONTRACTOR(){
        // GIVEN
        TimeRange rangeMorning = new TimeRange(LocalTime.of(9, 0), LocalTime.of(12, 0));
        SlotConfiguration slotConfigLocal = new SlotConfiguration(30,0);
        AvailabilityRule availabilityRule =
                AvailabilityRule.create(activeContractorID, weekDays, rangeMorning, slotConfigLocal);
        int numberOfSessionsExpected = 6;
        LocalDate day = LocalDate.of(2025,10, 8);

        LocalDateTime expectedStart = LocalDateTime.of(day, rangeMorning.startTime());
        LocalDateTime expectedEnd = LocalDateTime.of(day, rangeMorning.endTime());

        // WHEN
        List<TimeSlot> timeSlots = availabilityRule.generateSlotsForDay(day);

        // THEN
        assertEquals(numberOfSessionsExpected, timeSlots.size());
        assertEquals(expectedStart, timeSlots.get(0).startDateTime());
        assertEquals(expectedEnd,  timeSlots.getLast().startDateTime().plusMinutes(timeSlots.getLast().duration()));
    }



    @Test
    void SHOULD_ADD_BREAK_TIME_IN_A_DAY_PLANNING_FOR_CONTRACTOR(){
        // GIVEN
        TimeRange rangeMorning = new TimeRange(LocalTime.of(9, 0), LocalTime.of(12, 0));
        SlotConfiguration slotConfigLocal = new SlotConfiguration(30,0);
        AvailabilityRule availabilityRule =
                AvailabilityRule.create(activeContractorID, weekDays, rangeMorning, slotConfigLocal);
        int numberOfSessionsExpected = 4;
        LocalDate day = LocalDate.of(2025,10, 8);


        TimeRange breakTimeRange = new TimeRange(LocalTime.of(10, 30), LocalTime.of(11, 30));
        BreakTime breakTime = new BreakTime(breakTimeRange,null);

        availabilityRule.addBreakTime(breakTime);

        // WHEN
        List<TimeSlot> timeSlots = availabilityRule.generateSlotsForDay(day);

        // THEN
        assertEquals(numberOfSessionsExpected, timeSlots.size());
    }




    private static void printTimeSlots(List<TimeSlot> slots) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        if (slots.isEmpty()) {
            System.out.println("Aucun créneau disponible.");
            return;
        }

        System.out.println("Liste des créneaux :");
        for (int i = 0; i < slots.size(); i++) {
            TimeSlot slot = slots.get(i);
            LocalDateTime start = slot.startDateTime();
            LocalDateTime end = start.plusMinutes(slot.duration());

            System.out.printf(
                    "  #%d - %s → %s (%d minutes)%n",
                    i + 1,
                    start.format(formatter),
                    end.format(formatter),
                    slot.duration()
            );
        }
    }

}
