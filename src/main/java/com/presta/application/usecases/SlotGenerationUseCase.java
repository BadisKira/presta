package com.presta.application.usecases;

import com.presta.domain.model.valueobject.AvailableSlot;
import com.presta.domain.model.valueobject.TimeSlot;
import com.presta.domain.port.in.SlotGenerationUseCasePort;
import com.presta.domain.port.out.SlotGeneratorPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SlotGenerationUseCase implements SlotGenerationUseCasePort {

    private final SlotGeneratorPort slotGeneratorPort;

    public SlotGenerationUseCase(SlotGeneratorPort slotGeneratorPort) {
        this.slotGeneratorPort = slotGeneratorPort;
    }

    @Override
    public List<AvailableSlot> generateAvailableSlots(GenerateAvailableSlotsCommand command) {
        return slotGeneratorPort.generateAvailableSlots(
                command.contractorId(),
                command.startDate(),
                command.endDate());
    }

    @Override
    public List<TimeSlot> findOnlyAvailableSlots(AvailableSlotsQuery query) {
        return slotGeneratorPort.findOnlyAvailableSlots(
                query.contractorId(),
                query.startDate(),
                query.endDate());
    }

    @Override
    public boolean isSlotAvailable(CheckSlotAvailabilityCommand command) {
        return slotGeneratorPort.isSlotAvailable(
                command.contractorId(),
                command.startDateTime(),
                command.duration());
    }
}

