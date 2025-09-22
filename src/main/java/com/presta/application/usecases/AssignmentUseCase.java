package com.presta.application.usecases;

import com.presta.domain.exception.AssignmentNotFoundException;
import com.presta.domain.model.Assignment;
import org.springframework.data.domain.Sort;
import com.presta.domain.port.out.AssignmentPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Component;


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

    public Page<Assignment> getAssignments(String searchName , Pageable pageable) {
        return assignmentPort.findAssignments(searchName , pageable);
    }

    public Assignment updateAssignment(Assignment assignment){
        return  assignmentPort.save(assignment);
    }

    public void deleteAssignment(UUID id) {
        // Optionally verify existence first
        if (assignmentPort.findById(id).isEmpty()) {
            throw new AssignmentNotFoundException(id);
        }
        assignmentPort.deleteById(id);
    }


    public Page<Assignment> searchAssignments(AssignmentPort.AssignmentSearchCriteria criteria) {
        // Création de la pagination et tri
        Sort sort = createSort(criteria.sortBy(),criteria.sortDirection());
        Pageable pageable = PageRequest.of(criteria.page(), criteria.size(), sort);

        return assignmentPort.findAssignments(
                criteria.searchName(),
                pageable
        );
    }


    // private methode
    private Sort createSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "id"; // Tri par défaut
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return Sort.by(direction, sortBy);
    }




}
