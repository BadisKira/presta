package com.presta.domain.port.in.availability;

import com.presta.domain.model.UnavailabilityRule;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface UnavailabilityQueryPort {
    List<UnavailabilityRule> findByContractorIdAndPeriod(UUID id , LocalDate startDate, LocalDate endDate);

}
