package com.presta.infrastructure.persistence.repositories.user;

import com.presta.infrastructure.persistence.entities.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaClientRepository  extends JpaRepository<ClientEntity, UUID>, JpaSpecificationExecutor<ClientEntity> {
}