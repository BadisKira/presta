package com.presta.application.config;


import com.presta.domain.service.AppointmentDomainService;
import com.presta.domain.service.ContractorScheduleService;
import com.presta.application.usecases.SchedulingUseCase;
import com.presta.domain.port.UserProfilePort;
import com.presta.domain.port.UserRepositoryPort;
import com.presta.domain.port.UserSyncPort;
import com.presta.domain.service.UserProfileDomainService;
import com.presta.domain.service.UserSyncDomainService;
import com.presta.infrastructure.persistence.adapters.UserRepositoryAdapter;
import com.presta.infrastructure.persistence.adapters.UnavailabilityRepositoryAdapter;
import com.presta.infrastructure.persistence.mapper.AvailabilityRuleMapper;
import com.presta.infrastructure.persistence.mapper.UnavailabilityRuleMapper;
import com.presta.infrastructure.persistence.repositories.JpaAppointmentRepository;
import com.presta.infrastructure.persistence.repositories.JpaAvailabilityRuleRepository;
import com.presta.infrastructure.persistence.repositories.JpaContractorRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfiguration {
    @Bean
    public UserProfilePort userProfileService(UserRepositoryPort userRepositoryPort) {
        return new UserProfileDomainService(userRepositoryPort);
    }

    @Bean
    public UserSyncPort userSyncService(UserRepositoryPort userRepositoryPort) {
        return new UserSyncDomainService(userRepositoryPort);
    }


    @Bean
    public ContractorScheduleService scheduleService(){
        return  new ContractorScheduleService();
    }

   @Bean
    public SchedulingUseCase schedulingUseCase(
            JpaAppointmentRepository jpaAppointmentRepository,
            JpaAvailabilityRuleRepository jpaAvailabilityRuleRepository,
            UnavailabilityRepositoryAdapter unavailabilityRepositoryAdapter,
            UnavailabilityRuleMapper unavailabilityRuleMapper,
            JpaContractorRepository jpaContractorRepository,
            ContractorScheduleService contractorScheduleService,
            AvailabilityRuleMapper availabilityRuleMapper,
            UserRepositoryAdapter userRepositoryAdapter
    ) {

        return new  SchedulingUseCase(
                 jpaAppointmentRepository,
                 jpaAvailabilityRuleRepository,
                unavailabilityRepositoryAdapter,
                 jpaContractorRepository,
                 contractorScheduleService,
                 availabilityRuleMapper,
                 userRepositoryAdapter,
                unavailabilityRuleMapper) ;

    }


    @Bean
    public AppointmentDomainService appointmentDomainService(){
        return new  AppointmentDomainService();
    }
}

