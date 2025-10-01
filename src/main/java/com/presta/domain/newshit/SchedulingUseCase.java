package com.presta.domain.newshit;

import com.presta.domain.exception.ContractorNotActiveException;
import com.presta.domain.exception.UserNotFoundException;
import com.presta.domain.model.Appointment;
import com.presta.domain.model.AvailabilityRule;
import com.presta.domain.model.Contractor;
import com.presta.domain.model.UnavailabilityRule;
import com.presta.domain.model.valueobject.AvailabilityStatus;
import com.presta.domain.model.valueobject.AvailableSlot;
import com.presta.domain.model.valueobject.TimeSlot;
import com.presta.domain.port.out.UnavailabilityRuleRepositoryPort;
import com.presta.infrastructure.persistence.adapters.UserRepositoryAdapter;
import com.presta.infrastructure.persistence.adapters.availability.UnavailabilityRepositoryAdapter;
import com.presta.infrastructure.persistence.entities.AvailabilityRuleEntity;
import com.presta.infrastructure.persistence.mapper.availability.AvailabilityRuleMapper;
import com.presta.infrastructure.persistence.mapper.availability.UnavailabilityRuleMapper;
import com.presta.infrastructure.persistence.repositories.JpaAppointmentRepository;
import com.presta.infrastructure.persistence.repositories.JpaUnavailabilityRuleRepository;
import com.presta.infrastructure.persistence.repositories.availability.JpaAvailabilityRuleRepository;
import com.presta.infrastructure.persistence.repositories.user.JpaContractorRepository;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;

// que doit contenu
// Pourvoir générer un planning à partir



interface ContractorScheduleUseCasePort {

    ContractorPlanning generatePlanning(
             UUID contractorId,
             LocalDate startDate,
             LocalDate endDate
    );
}

public class SchedulingUseCase implements ContractorScheduleUseCasePort {

    private final JpaAppointmentRepository jpaAppointmentRepository;
    private final JpaAvailabilityRuleRepository jpaAvailabilityRuleRepository;
    private final UnavailabilityRepositoryAdapter unavailabilityRepositoryAdapter;
    private final JpaContractorRepository jpaContractorRepository;
    private final ContractorScheduleService contractorScheduleService;
    private final AvailabilityRuleMapper availabilityRuleMapper;
    private final UserRepositoryAdapter userRepositoryAdapter;
    private final UnavailabilityRuleMapper unavailabilityRuleMapper;

    public SchedulingUseCase(JpaAppointmentRepository jpaAppointmentRepository, JpaAvailabilityRuleRepository jpaAvailabilityRuleRepository, UnavailabilityRepositoryAdapter unavailabilityRepositoryAdapter, JpaContractorRepository jpaContractorRepository, ContractorScheduleService contractorScheduleService, AvailabilityRuleMapper availabilityRuleMapper, UserRepositoryAdapter userRepositoryAdapter, UnavailabilityRuleMapper unavailabilityRuleMapper) {
        this.jpaAppointmentRepository = jpaAppointmentRepository;
        this.jpaAvailabilityRuleRepository = jpaAvailabilityRuleRepository;
        this.unavailabilityRepositoryAdapter = unavailabilityRepositoryAdapter;
        this.jpaContractorRepository = jpaContractorRepository;
        this.contractorScheduleService = contractorScheduleService;
        this.availabilityRuleMapper = availabilityRuleMapper;
        this.userRepositoryAdapter = userRepositoryAdapter;
        this.unavailabilityRuleMapper = unavailabilityRuleMapper;
    }


    @Override
    public ContractorPlanning generatePlanning(UUID contractorId,
                                               LocalDate startDate,
                                               LocalDate endDate) {

        Optional<Contractor> contractor = this.userRepositoryAdapter.findContractorById(contractorId);
        if(contractor.isEmpty()){
            throw new UserNotFoundException(contractorId);
        }

        List<AvailabilityRule> availabilityRuleList = this.jpaAvailabilityRuleRepository.findByIsActiveAndContractorId(contractorId).stream().map(
                this.availabilityRuleMapper::toDomain
        ).toList();

        List<UnavailabilityRule> unavailabilityRuleList = this.unavailabilityRepositoryAdapter.findByContractorIdAndDateRange(contractorId,startDate,endDate);
        List<Appointment> appointmentList = emptyList();



        return this.contractorScheduleService.generatePlanning(contractorId,
                availabilityRuleList.get(0),
                unavailabilityRuleList,
                appointmentList,
                startDate,
                endDate
                );
    }
}
