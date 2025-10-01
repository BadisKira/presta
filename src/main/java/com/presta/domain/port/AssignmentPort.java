package com.presta.domain.port;

import com.presta.domain.model.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssignmentPort {


    record AssignmentSearchCriteria(
            String searchName,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {}

    Assignment save(Assignment assignment);
    Optional<Assignment> findById(UUID id);
    List<Assignment> findAll();
    void deleteById(UUID id);
    boolean exists(UUID id);
    Page<Assignment> findAssignments(String searchName, Pageable pageable);
}