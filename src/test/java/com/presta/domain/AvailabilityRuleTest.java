package com.presta.domain;

import com.presta.domain.model.AvailabilityRule;
import com.presta.domain.model.valueobject.SlotConfiguration;
import com.presta.domain.model.valueobject.TimeRange;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;


@ActiveProfiles("test")
public class AvailabilityRuleTest {

    UUID contractorId = UUID.randomUUID();
    Set<DayOfWeek> weekDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
    TimeRange range = new TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0));
    SlotConfiguration slotConfig = new SlotConfiguration(30,10);


    // CONTRACTOR THAT IS ACTIF
    UUID activeContractorID = UUID.randomUUID();
    // CONTRACTOR THAT IS NOOOOT ACTIF
    UUID notActiveContractorID = UUID.randomUUID();

    //AvailabilityRule activeContractorAvailabilityRule = AvailabilityRule.create(activeContractorID, true, weekDays, range, slotConfig);



    @Test
    void SHOULD_NOT_CREATE_AVAILABILITY_RULE() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            AvailabilityRule.create(notActiveContractorID, false, weekDays, range, slotConfig);
        });

        assertEquals("Le prestataire doit etre actif", exception.getMessage());
    }

    @Test
    void SHOULD_CREATE_AVAILABILITY_RULE() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            AvailabilityRule.create(notActiveContractorID, false, weekDays, range, slotConfig);
        });

        assertEquals("Le prestataire doit etre actif", exception.getMessage());
    }

}
