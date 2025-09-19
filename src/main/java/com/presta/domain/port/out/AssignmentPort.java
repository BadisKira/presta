package com.presta.domain.port.out;

import com.presta.domain.model.Assignment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssignmentPort {
    Assignment save(Assignment assignment);
    Optional<Assignment> findById(UUID id);
    List<Assignment> findAll();
    void deleteById(UUID id);
    boolean exists(UUID id);
}