package com.presta.infrastructure.persistence.repositories;

import com.presta.infrastructure.persistence.entities.UnavailabilityRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Repository
public interface JpaUnavailabilityRuleRepository extends JpaRepository<UnavailabilityRuleEntity, UUID> {


    @Query("""
        SELECT u FROM UnavailabilityRuleEntity u
        WHERE u.contractorId = :contractorId
        AND u.startDate <= :endDate
        AND u.endDate >= :startDate
        ORDER BY u.startDate ASC, u.startTime ASC
        """)
    List<UnavailabilityRuleEntity> findByContractorIdAndDateRange(
            @Param("contractorId") UUID contractorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

//    List<UnavailabilityRuleEntity> findByContractorIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAscStartTimeAsc(
//            UUID contractorId,
//            LocalDate endDate,
//            LocalDate startDate
//    );


}
