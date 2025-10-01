package com.presta.infrastructure.persistence.repositories;

import com.presta.infrastructure.persistence.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByKeycloakId(UUID keycloakId);
    boolean existsByKeycloakId(UUID keycloakId);
    Optional<UserEntity> findByEmail(String email);


    @Modifying
    @Query("""
       UPDATE UserEntity u
       SET u.isActive = ?2
       WHERE u.id = ?1
   """)
    int changeActivationState(UUID uuid, boolean active);


    @Query("""
            Select u.isActive
            From UserEntity u
            WHERE u.id = ?1
            """)
    boolean isUserActive(UUID id);


}