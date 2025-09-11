package com.presta.domain.port.out;

import com.presta.domain.models.Assignment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssignmentRepository {
    Assignment save(Assignment assignment);
    Optional<Assignment> findById(UUID id);
    List<Assignment> findAll();
    void deleteById(UUID id);
}