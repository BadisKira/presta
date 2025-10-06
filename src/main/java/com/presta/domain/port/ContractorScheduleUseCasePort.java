package com.presta.domain.port;

import com.presta.domain.model.ContractorPlanning;

import java.time.LocalDate;
import java.util.UUID;

public interface ContractorScheduleUseCasePort {

    ContractorPlanning generatePlanning(
            UUID contractorId,
            LocalDate startDate,
            LocalDate endDate
    );
}
