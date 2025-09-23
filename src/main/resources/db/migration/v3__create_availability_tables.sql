CREATE TABLE presta.availability_rule (
    id UUID PRIMARY KEY DEFAULT ,
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
        REFERENCES presta.contractorAccount(id) ON DELETE CASCADE,

    CONSTRAINT check_time_coherence
        CHECK (end_time > start_time),

    CONSTRAINT check_slot_duration
        CHECK (slot_duration > 0 AND slot_duration <= 480),

    CONSTRAINT check_week_days
        CHECK (week_days <@ ARRAY[1,2,3,4,5,6,7])
);

CREATE TABLE presta.unavailability_rule (
    id UUID PRIMARY KEY DEFAULT ,
    contractor_id UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    start_time TIME, -- NULL = journée complète
    end_time TIME,   -- NULL = journée complète
    reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_contractor_unavailability
        FOREIGN KEY (contractor_id)
        REFERENCES presta.contractorAccount(id) ON DELETE CASCADE,

    CONSTRAINT check_date_coherence
        CHECK (end_date >= start_date),

    CONSTRAINT check_time_unavailability
        CHECK (
            (start_time IS NULL AND end_time IS NULL) OR
            (start_time IS NOT NULL AND end_time IS NOT NULL AND end_time > start_time)
        )
);

CREATE TABLE presta.break_time (
    id UUID PRIMARY KEY DEFAULT ,
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
--
--CREATE TABLE presta.appointment (
--    id UUID PRIMARY KEY DEFAULT ,
--    contractor_id UUID NOT NULL,
--    client_id UUID NOT NULL,
--    appointment_datetime TIMESTAMP NOT NULL,
--    duration INTEGER NOT NULL, -- en minutes
--    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
--    reason TEXT,
--    notes TEXT,
--    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--
--    CONSTRAINT fk_appointment_contractor
--        FOREIGN KEY (contractor_id)
--        REFERENCES presta.contractorAccount(id),
--
--    CONSTRAINT fk_appointment_client
--        FOREIGN KEY (client_id)
--        REFERENCES presta.clientAccount(id),
--
--    CONSTRAINT check_appointment_status
--        CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
--
--    CONSTRAINT check_appointment_duration
--        CHECK (duration > 0 AND duration <= 480),
--
--    CONSTRAINT unique_contractor_datetime
--        UNIQUE (contractor_id, appointment_datetime)
--);

CREATE INDEX idx_availability_contractor ON presta.availability_rule(contractor_id);
CREATE INDEX idx_unavailability_contractor ON presta.unavailability_rule(contractor_id);
CREATE INDEX idx_unavailability_dates ON presta.unavailability_rule(start_date, end_date);
--CREATE INDEX idx_appointment_contractor ON presta.appointment(contractor_id);
--CREATE INDEX idx_appointment_client ON presta.appointment(client_id);
--CREATE INDEX idx_appointment_datetime ON presta.appointment(appointment_datetime);
--CREATE INDEX idx_appointment_status ON presta.appointment(status);

--CREATE OR REPLACE FUNCTION presta.update_updated_at_column()
--RETURNS TRIGGER AS $$
--BEGIN
--    NEW.updated_at = CURRENT_TIMESTAMP;
--    RETURN NEW;
--END;
--$$ LANGUAGE plpgsql;
--
--CREATE TRIGGER update_availability_rule_updated_at
--    BEFORE UPDATE ON presta.availability_rule
--    FOR EACH ROW
--    EXECUTE FUNCTION presta.update_updated_at_column();
--
--CREATE TRIGGER update_appointment_updated_at
--    BEFORE UPDATE ON presta.appointment
--    FOR EACH ROW
--    EXECUTE FUNCTION presta.update_updated_at_column();