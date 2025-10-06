package com.presta.infrastructure.web.controllers;


import com.presta.domain.model.UnavailabilityRule;
import com.presta.infrastructure.persistence.adapters.UnavailabilityRepositoryAdapter;
import com.presta.infrastructure.web.dtos.availability.CreateUnavailabilityRuleRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/unavailabilityRules")
public class UnavailabilityRuleController {
    private final UnavailabilityRepositoryAdapter unavailabilityRepositoryAdapter;

    public UnavailabilityRuleController(UnavailabilityRepositoryAdapter unavailabilityRepositoryAdapter) {
        this.unavailabilityRepositoryAdapter = unavailabilityRepositoryAdapter;
    }

    @PostMapping
    public ResponseEntity<UnavailabilityRule> save(@RequestBody CreateUnavailabilityRuleRequest request) {
        return new ResponseEntity<>(this.unavailabilityRepositoryAdapter.save(
                request.toDomain()
        ), HttpStatusCode.valueOf(201));
    }
}
