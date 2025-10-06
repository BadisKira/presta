package com.presta.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.presta.TestcontainersConfiguration;
import com.presta.domain.model.Contractor;
import com.presta.domain.model.valueobject.ContactInfo;
import com.presta.domain.model.valueobject.KeycloakUserId;
import com.presta.domain.model.valueobject.UserProfile;
import com.presta.domain.port.UserSyncPort;
import com.presta.domain.service.UserSyncDomainService;
import com.presta.infrastructure.web.dtos.appointment.CreateAppointmentRequest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.presta.bootstrap.PrestaProjectApplication.class)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Sql(scripts = "/test-data/setup-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class AppointmentControllerIT {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    // IDs correspondant à ceux du script test-data.sql
    private static final UUID CONTRACTOR_ID = UUID.fromString("41e9240a-d656-435f-80d3-731ec3c3f501");
    private static final UUID CLIENT_ID = UUID.fromString("7b3e9f12-8a45-4c3d-9e1f-5d8c2a1b6f90");

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldCreateAppointmentSuccessfully() throws Exception {
        // Arrange
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                CONTRACTOR_ID,
                CLIENT_ID,
                LocalDateTime.of(2025, 10, 13, 14, 30, 0),
                30,
                "Consultation initiale",
                "Client préfère être contacté par email"
        );

        // Act & Assert
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.contractorId").value(CONTRACTOR_ID.toString()))
                .andExpect(jsonPath("$.clientId").value(CLIENT_ID.toString()))
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }
}




