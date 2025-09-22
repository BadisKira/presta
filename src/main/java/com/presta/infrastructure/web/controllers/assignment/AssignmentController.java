package com.presta.infrastructure.web.controllers.assignment;

import com.presta.application.usecases.AssignmentUseCase;
import com.presta.domain.model.Assignment;
import com.presta.domain.port.out.AssignmentPort;
import com.presta.infrastructure.web.dtos.assignment.CreateAssignmentRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentUseCase assignmentUseCase;

    public AssignmentController(AssignmentUseCase assignmentUseCase) {
        this.assignmentUseCase = assignmentUseCase;
    }

    @GetMapping
    public ResponseEntity<Page<Assignment>> list(
            @RequestParam(required = false) String searchName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {

        AssignmentPort.AssignmentSearchCriteria criteria =
                new AssignmentPort.AssignmentSearchCriteria(
                        searchName, page, size, sortBy, sortDirection
                );

        Page<Assignment> assignments = assignmentUseCase.searchAssignments(criteria);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/{id}")
    public Assignment getOne(@PathVariable UUID id) {
        return assignmentUseCase.getAssignment(id);
    }

    @PostMapping
    public ResponseEntity<Assignment> create(@RequestBody CreateAssignmentRequest request) {
        Assignment createdAssignment = assignmentUseCase.createAssignment(request.name(), request.description());
        return new ResponseEntity<>(createdAssignment, HttpStatusCode.valueOf(201));
    }

    @PutMapping
    public ResponseEntity<Assignment> create(@RequestBody Assignment request) {
        Assignment updatedAssignment = assignmentUseCase.updateAssignment(request);
        return new ResponseEntity<>(updatedAssignment, HttpStatusCode.valueOf(204));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        assignmentUseCase.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }
}
