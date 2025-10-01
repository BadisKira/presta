package com.presta.application.usecases;

import com.presta.domain.exception.UserNotFoundException;
import com.presta.domain.model.*;
import com.presta.domain.port.ContractorScheduleUseCasePort;
import com.presta.domain.service.ContractorScheduleService;
import com.presta.infrastructure.persistence.adapters.UserRepositoryAdapter;
import com.presta.infrastructure.persistence.adapters.UnavailabilityRepositoryAdapter;
import com.presta.infrastructure.persistence.mapper.AvailabilityRuleMapper;
import com.presta.infrastructure.persistence.mapper.UnavailabilityRuleMapper;
import com.presta.infrastructure.persistence.repositories.JpaAppointmentRepository;
import com.presta.infrastructure.persistence.repositories.JpaAvailabilityRuleRepository;
import com.presta.infrastructure.persistence.repositories.JpaContractorRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;

@Component
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
