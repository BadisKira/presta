package com.presta.infrastructure.persistence.mapper;


import com.presta.domain.model.UnavailabilityRule;
import com.presta.domain.model.valueobject.UnavailabilityPeriod;
import com.presta.infrastructure.persistence.entities.UnavailabilityRuleEntity;
import org.springframework.stereotype.Component;

@Component
public class UnavailabilityRuleMapper {

    /**
     * Convertit une entité JPA vers un objet domain
     */
    public UnavailabilityRule toDomain(UnavailabilityRuleEntity entity) {
        if (entity == null) {
            return null;
        }

        UnavailabilityPeriod period = new UnavailabilityPeriod(
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getStartTime(),
                entity.getEndTime()
        );

        return new UnavailabilityRule(
                entity.getId(),
                entity.getContractorId(),
                period,
                entity.getReason(),
                entity.getCreatedAt()
        );
    }


    public UnavailabilityRuleEntity toEntity(UnavailabilityRule domain) {
        if (domain == null) {
            return null;
        }

        return new UnavailabilityRuleEntity(
                domain.getId(),
                domain.getContractorId(),
                domain.getStartDate(),
                domain.getEndDate(),
                domain.getStartTime(),
                domain.getEndTime(),
                domain.getReason(),
                domain.getCreatedAt()
        );
    }


    public void updateEntity(UnavailabilityRule domain, UnavailabilityRuleEntity entity) {
        if (domain == null || entity == null) {
            return;
        }

        entity.setContractorId(domain.getContractorId());
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
        entity.setStartTime(domain.getStartTime());
        entity.setEndTime(domain.getEndTime());
        entity.setReason(domain.getReason());
        entity.setCreatedAt(domain.getCreatedAt());
    }
}
