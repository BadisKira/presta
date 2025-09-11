package com.presta.infrastructure.controllers;

import com.presta.application.dtos.CreateAssignmentRequest;
import com.presta.application.usecases.AssignmentUseCase;
import com.presta.domain.models.Assignment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentUseCase assignmentUseCase;

    public AssignmentController(AssignmentUseCase assignmentUseCase) {
        this.assignmentUseCase = assignmentUseCase;
    }

    @GetMapping
    public List<Assignment> list() {
        return assignmentUseCase.listServices();
    }

    @GetMapping("/{id}")
    public Assignment getOne(@PathVariable UUID id) {
        return assignmentUseCase.getService(id);
    }

    @PostMapping
    public ResponseEntity<Assignment> create(@RequestBody CreateAssignmentRequest request) {
        Assignment created = assignmentUseCase.createService(request.name(), request.description());
        return ResponseEntity.created(URI.create("/api/assignments/" + created.id())).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        assignmentUseCase.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}
