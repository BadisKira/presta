package com.presta.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "contractorAccount", schema = "presta")
public class ContractorEntity {

    @Id
    private UUID id; // Même ID que UserEntity

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "assignment_id")
    private UUID assignmentId;

    @Column(name = "speciality", length = 100)
    private String speciality;

    // Constructeurs
    public ContractorEntity() {}

    public ContractorEntity(UUID id, String fullName, String address, UUID assignmentId, String speciality) {
        this.id = id;
        this.fullName = fullName;
        this.address = address;
        this.assignmentId = assignmentId;
        this.speciality = speciality;
    }

    // Getters et Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public UUID getAssignmentId() { return assignmentId; }
    public void setAssignmentId(UUID assignmentId) { this.assignmentId = assignmentId; }

    public String getSpeciality() { return speciality; }
    public void setSpeciality(String speciality) { this.speciality = speciality; }
}
