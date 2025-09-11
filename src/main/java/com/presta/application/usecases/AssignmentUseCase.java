package com.presta.application.usecases;

import com.presta.domain.exceptions.AssignmentNotFoundException;
import com.presta.domain.models.Assignment;
import com.presta.domain.port.out.AssignmentRepository;
import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
public class AssignmentUseCase {

    private final AssignmentRepository assignmentRepository;

    public AssignmentUseCase(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    public Assignment createService(String name, String description) {
        UUID id = UUID.randomUUID();
        Assignment assignment = new Assignment(id, name, description);
        return assignmentRepository.save(assignment);
    }

    public Assignment getService(UUID id) {
        return assignmentRepository.findById(id).orElseThrow(() -> new AssignmentNotFoundException(id));
    }

    public List<Assignment> listServices() {
        return assignmentRepository.findAll();
    }

    public void deleteService(UUID id) {
        // Optionally verify existence first
        if (assignmentRepository.findById(id).isEmpty()) {
            throw new AssignmentNotFoundException(id);
        }
        assignmentRepository.deleteById(id);
    }
}
