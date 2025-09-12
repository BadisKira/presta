package com.presta.infrastructure.persistence.adapters;

import com.presta.domain.model.Assignment;
import com.presta.domain.port.out.AssignmentPort;
import com.presta.infrastructure.persistence.entities.AssignmentEntity;
import com.presta.infrastructure.persistence.repositories.JpaAssignmentRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Transactional
public class AssignmentRepositoryAdapter implements AssignmentPort {

    private final JpaAssignmentRepository jpaRepo;

    public AssignmentRepositoryAdapter(JpaAssignmentRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    private Assignment toDomain(AssignmentEntity e) {
        return new Assignment(e.getId(), e.getName(), e.getDescription());
    }

    private AssignmentEntity toEntity(Assignment s) {
        AssignmentEntity e = new AssignmentEntity();
        e.setId(s.id());
        e.setName(s.name());
        e.setDescription(s.description());
        return e;
    }

    @Override
    public Assignment save(Assignment assignment) {
        if (assignment.id() == null) {
            Assignment withId = new Assignment(UUID.randomUUID(), assignment.name(), assignment.description());
            AssignmentEntity saved = jpaRepo.save(toEntity(withId));
            return toDomain(saved);
        } else {
            AssignmentEntity entity = toEntity(assignment);
            AssignmentEntity saved = jpaRepo.save(entity);
            return toDomain(saved);
        }
    }

    @Override
    public Optional<Assignment> findById(UUID id) {
        return jpaRepo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Assignment> findAll() {
        return jpaRepo.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepo.deleteById(id);
    }
}
