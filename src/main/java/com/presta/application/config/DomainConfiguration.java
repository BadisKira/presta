package com.presta.application.config;


import com.presta.domain.exception.DomainException;
import com.presta.domain.port.in.UserProfilePort;
import com.presta.domain.port.in.UserRegistrationPort;
import com.presta.domain.port.in.UserSyncPort;
import com.presta.domain.port.out.UserRepositoryPort;
import com.presta.domain.service.UserProfileDomainService;
import com.presta.domain.service.UserRegistrationDomainService;
import com.presta.domain.service.UserSyncDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfiguration {
    @Bean
    public UserRegistrationPort userRegistrationService(UserRepositoryPort userRepositoryPort) {
        return new UserRegistrationDomainService(userRepositoryPort);
    }

    @Bean
    public UserProfilePort userProfileService(UserRepositoryPort userRepositoryPort) {
        return new UserProfileDomainService(userRepositoryPort);
    }

    @Bean
    public UserSyncPort userSyncService(UserRepositoryPort userRepositoryPort) {
        return new UserSyncDomainService(userRepositoryPort);
    }


}
