package com.presta.infrastructure.persistence.entities;

import com.presta.domain.model.Assignment;
import jakarta.persistence.*;
import java.util.UUID;
import java.util.Objects;

@Entity
@Table(name = "contractor_account", schema = "presta")
public class ContractorEntity {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private UserEntity user;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignment_id")
    private AssignmentEntity assignment;

    @Column(name = "speciality", length = 100)
    private String speciality;

    // Constructeurs
    public ContractorEntity() {}

    public ContractorEntity(UserEntity user, String fullName, String address,
                            AssignmentEntity assignment, String speciality) {
        this.user = user;
        this.fullName = fullName;
        this.address = address;
        this.assignment = assignment;
        this.speciality = speciality;
    }




    public UUID getAssignmentId() {
        return assignment != null ? assignment.getId() : null;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public AssignmentEntity getAssignment() {
        return assignment;
    }

    public void setAssignment(AssignmentEntity assignment) {
        this.assignment = assignment;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContractorEntity that = (ContractorEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ContractorEntity{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", address='" + address + '\'' +
                ", assignmentId=" + getAssignmentId() +
                ", speciality='" + speciality + '\'' +
                '}';
    }
}