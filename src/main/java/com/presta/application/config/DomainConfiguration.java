package com.presta.application.config;


import com.presta.domain.newshit.ContractorScheduleService;
import com.presta.domain.newshit.SchedulingUseCase;
import com.presta.domain.port.in.UserProfilePort;
import com.presta.domain.port.in.UserSyncPort;
import com.presta.domain.port.out.*;
import com.presta.domain.service.UserProfileDomainService;
import com.presta.domain.service.UserSyncDomainService;
import com.presta.infrastructure.persistence.adapters.UserRepositoryAdapter;
import com.presta.infrastructure.persistence.adapters.availability.UnavailabilityRepositoryAdapter;
import com.presta.infrastructure.persistence.mapper.availability.AvailabilityRuleMapper;
import com.presta.infrastructure.persistence.mapper.availability.UnavailabilityRuleMapper;
import com.presta.infrastructure.persistence.repositories.JpaAppointmentRepository;
import com.presta.infrastructure.persistence.repositories.JpaUnavailabilityRuleRepository;
import com.presta.infrastructure.persistence.repositories.availability.JpaAvailabilityRuleRepository;
import com.presta.infrastructure.persistence.repositories.user.JpaContractorRepository;
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
}

