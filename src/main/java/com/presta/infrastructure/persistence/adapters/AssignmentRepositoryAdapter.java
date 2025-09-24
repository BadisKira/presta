package com.presta.infrastructure.persistence.adapters;

import com.presta.domain.model.Assignment;
import com.presta.domain.port.AssignmentPort;
import com.presta.infrastructure.persistence.entities.AssignmentEntity;
import com.presta.infrastructure.persistence.repositories.JpaAssignmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Transactional
public class AssignmentRepositoryAdapter implements AssignmentPort {

    private final JpaAssignmentRepository jpaAssignmentRepository;

    public AssignmentRepositoryAdapter(JpaAssignmentRepository jpaAssignmentRepository) {
        this.jpaAssignmentRepository = jpaAssignmentRepository;
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
            AssignmentEntity saved = jpaAssignmentRepository.save(toEntity(withId));
            return toDomain(saved);
        } else {
            AssignmentEntity entity = toEntity(assignment);
            AssignmentEntity saved = jpaAssignmentRepository.save(entity);
            return toDomain(saved);
        }
    }

    @Override
    public Optional<Assignment> findById(UUID id) {
        return jpaAssignmentRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Assignment> findAll() {
        return jpaAssignmentRepository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaAssignmentRepository.deleteById(id);
    }

    @Override
    public boolean exists(UUID id) {
        return jpaAssignmentRepository.existsById(id);
    }


    @Override
    public Page<Assignment> findAssignments(String searchName, Pageable pageable) {
        Specification<AssignmentEntity> spec = Specification.where(null);

        if (searchName != null && !searchName.isBlank()) {
            spec = spec.and((root, query, cb) -> {
                return cb.like(cb.lower(root.get("name")), "%" + searchName.toLowerCase() + "%");
            });
        }

        Page<AssignmentEntity> entityPage = jpaAssignmentRepository.findAll(spec, pageable);
        return entityPage.map(this::toDomain);
    }
}
