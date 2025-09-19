package com.presta.application.usecases;

import com.presta.domain.exception.AssignmentNotFoundException;
import com.presta.domain.model.Assignment;
import com.presta.domain.port.out.AssignmentPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AssignmentUseCase {

    private final AssignmentPort assignmentPort;

    public AssignmentUseCase(AssignmentPort assignmentPort) {
        this.assignmentPort = assignmentPort;
    }

    public Assignment createAssignment(String name, String description) {
        UUID id = UUID.randomUUID();
        Assignment assignment = new Assignment(id, name, description);
        return assignmentPort.save(assignment);
    }

    public Assignment getAssignment(UUID id) {
        return assignmentPort.findById(id).orElseThrow(() -> new AssignmentNotFoundException(id));
    }

    public List<Assignment> listAssignments() {
        return assignmentPort.findAll();
    }

    public void deleteAssignment(UUID id) {
        // Optionally verify existence first
        if (assignmentPort.findById(id).isEmpty()) {
            throw new AssignmentNotFoundException(id);
        }
        assignmentPort.deleteById(id);
    }
}
