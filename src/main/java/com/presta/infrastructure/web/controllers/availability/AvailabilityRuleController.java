package com.presta.infrastructure.web.controllers.availability;

import com.presta.domain.model.AvailabilityRule;
import com.presta.domain.model.valueobject.AvailableSlot;
import com.presta.domain.model.valueobject.TimeSlot;
import com.presta.domain.port.in.SlotGenerationUseCasePort;
import com.presta.infrastructure.persistence.adapters.availability.AvailabilityRepositoryAdapter;
import com.presta.infrastructure.web.dtos.availability.CreateAvailabilityRuleRequest;
import com.presta.infrastructure.web.dtos.availability.CreateBeakTimeRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/availabilityRules")
public class AvailabilityRuleController{

    private final AvailabilityRepositoryAdapter availabilityRepositoryAdapter;
    private final SlotGenerationUseCasePort slotGenerationUseCase;

    public AvailabilityRuleController(AvailabilityRepositoryAdapter availabilityRepositoryAdapter, SlotGenerationUseCasePort slotGenerationUseCase) {
        this.availabilityRepositoryAdapter = availabilityRepositoryAdapter;
        this.slotGenerationUseCase = slotGenerationUseCase;
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<AvailabilityRule>> getAvailabilityRulesByContractorID(@PathVariable UUID contractorId){
        return ResponseEntity.ok(this.availabilityRepositoryAdapter.findActiveByContractorId(contractorId));
    }

    @PostMapping
    public ResponseEntity<AvailabilityRule> saveAvailabilityRule(@RequestBody CreateAvailabilityRuleRequest createAvailabilityRuleRequest){
            return new ResponseEntity<AvailabilityRule>(
                    this.availabilityRepositoryAdapter.save(
                            createAvailabilityRuleRequest.toDomain()
                    ),
                    HttpStatusCode.valueOf(201)
            );
    }

    @PatchMapping
    ResponseEntity<AvailabilityRule> addBreakTime(@RequestBody CreateBeakTimeRequest createBeakTimeRequest){
        return null;
    }


    @GetMapping("")
    ResponseEntity<List<TimeSlot>> getAvailableTimeSlots(@PathVariable UUID contractorId){
        return null;
    }

    @GetMapping("/timeslots")
    public List<AvailableSlot> getAvailableSlots(
            @RequestParam UUID contractorId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        return slotGenerationUseCase.generateAvailableSlots(
                new SlotGenerationUseCasePort.GenerateAvailableSlotsCommand(contractorId, startDate, endDate));
    }


    @GetMapping("/free")
    public List<TimeSlot> getFreeSlots(
            @RequestParam UUID contractorId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        return slotGenerationUseCase.findOnlyAvailableSlots(
                new SlotGenerationUseCasePort.AvailableSlotsQuery(contractorId, startDate, endDate));
    }


    @GetMapping("/check")
    public boolean checkSlotAvailability(
            @RequestParam UUID contractorId,
            @RequestParam LocalDateTime startDateTime,
            @RequestParam int duration) {

        return slotGenerationUseCase.isSlotAvailable(
                new SlotGenerationUseCasePort.CheckSlotAvailabilityCommand(contractorId, startDateTime, duration));
    }

}
