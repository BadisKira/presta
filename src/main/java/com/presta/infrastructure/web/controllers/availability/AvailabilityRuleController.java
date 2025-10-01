package com.presta.infrastructure.web.controllers.availability;

import com.presta.domain.model.AvailabilityRule;
import com.presta.infrastructure.persistence.adapters.AvailabilityRepositoryAdapter;
import com.presta.infrastructure.web.dtos.availability.CreateAvailabilityRuleRequest;
import com.presta.infrastructure.web.dtos.availability.CreateBeakTimeRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/availabilityRules")
public class AvailabilityRuleController{

    private final AvailabilityRepositoryAdapter availabilityRepositoryAdapter;

    public AvailabilityRuleController(AvailabilityRepositoryAdapter availabilityRepositoryAdapter) {
        this.availabilityRepositoryAdapter = availabilityRepositoryAdapter;
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

}
