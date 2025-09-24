CREATE TABLE presta.availability_rule (
    id UUID PRIMARY KEY  ,
    contractor_id UUID NOT NULL,
    week_days INTEGER[] NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_duration INTEGER NOT NULL, -- en minutes
    rest_time INTEGER DEFAULT 0, -- temps entre RDV en minutes
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_contractor_availability
        FOREIGN KEY (contractor_id)
        REFERENCES presta.contractor_account(id) ON DELETE CASCADE,

    CONSTRAINT check_time_coherence
        CHECK (end_time > start_time),

    CONSTRAINT check_slot_duration
        CHECK (slot_duration > 0 AND slot_duration <= 480),

    CONSTRAINT check_week_days
        CHECK (week_days <@ ARRAY[1,2,3,4,5,6,7])
);

CREATE TABLE presta.break_time (
    id UUID PRIMARY KEY  ,
    availability_rule_id UUID NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    week_days INTEGER[], -- NULL = tous les jours de la règle

    CONSTRAINT fk_availability_rule
        FOREIGN KEY (availability_rule_id)
        REFERENCES presta.availability_rule(id) ON DELETE CASCADE,

    CONSTRAINT check_break_time
        CHECK (end_time > start_time)
);

CREATE TABLE presta.unavailability_rule (
    id UUID PRIMARY KEY  ,
    contractor_id UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    start_time TIME, -- NULL = journée complète
    end_time TIME,   -- NULL = journée complète
    reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_contractor_unavailability
        FOREIGN KEY (contractor_id)
        REFERENCES presta.contractor_account(id) ON DELETE CASCADE,

    CONSTRAINT check_date_coherence
        CHECK (end_date >= start_date),

    CONSTRAINT check_time_unavailability
        CHECK (
            (start_time IS NULL AND end_time IS NULL) OR
            (start_time IS NOT NULL AND end_time IS NOT NULL AND end_time > start_time)
        )
);




CREATE INDEX idx_availability_contractor ON presta.availability_rule(contractor_id);
CREATE INDEX idx_unavailability_contractor ON presta.unavailability_rule(contractor_id);
CREATE INDEX idx_unavailability_dates ON presta.unavailability_rule(start_date, end_date);
