package com.presta.infrastructure.persistence.entities;

import com.presta.domain.model.valueobject.AppointmentStatus;
import jakarta.annotation.Generated;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "appointment", schema = "presta")
public class AppointmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(name = "contractor_id", nullable = false)
    private UUID contractorId;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "appointment_datetime", nullable = false)
    private LocalDateTime appointmentDateTime;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppointmentStatus status;


    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = true)
    private LocalDateTime updatedAt;



    @Column(name = "reason")
    private String reason;

    @Column(name = "notes")
    private String notes;


    public UUID getContractorId() {
        return contractorId;
    }

    public void setContractorId(UUID contractorId) {
        this.contractorId = contractorId;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AppointmentEntity that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(contractorId, that.contractorId) && Objects.equals(clientId, that.clientId) && Objects.equals(appointmentDateTime, that.appointmentDateTime) && Objects.equals(duration, that.duration) && Objects.equals(status, that.status) && Objects.equals(reason, that.reason) && Objects.equals(notes, that.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, contractorId, clientId, appointmentDateTime, duration, status, reason, notes);
    }

    @Override
    public String toString() {
        return "AppointmentEntity{" +
                "id=" + id +
                ", contractorId=" + contractorId +
                ", clientId=" + clientId +
                ", appointmentDateTime=" + appointmentDateTime +
                ", duration=" + duration +
                ", status='" + status + '\'' +
                ", reason='" + reason + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}