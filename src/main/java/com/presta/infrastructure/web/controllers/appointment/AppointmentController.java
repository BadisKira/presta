package com.presta.infrastructure.web.controllers.appointment;


import com.presta.application.usecases.AppointmentUseCase;
import com.presta.domain.model.Appointment;
import com.presta.infrastructure.persistence.adapters.AppointmentRepositoryAdapter;
import com.presta.infrastructure.web.dtos.appointment.CreateAppointmentRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentUseCase appointmentUseCase;

    public AppointmentController(AppointmentUseCase appointmentUseCase) {
        this.appointmentUseCase = appointmentUseCase;
    }


    @PostMapping
    public ResponseEntity<Appointment> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request) {
        Appointment appointment = request.toDomain();
        Appointment saved = appointmentUseCase.bookAppointment(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

}
