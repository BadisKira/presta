CREATE TABLE presta.appointment (
    id UUID PRIMARY KEY  ,
    contractor_id UUID NOT NULL,
    client_id UUID NOT NULL,
    appointment_datetime TIMESTAMP NOT NULL,
    duration INTEGER NOT NULL, -- en minutes
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reason TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_appointment_contractor
        FOREIGN KEY (contractor_id)
        REFERENCES presta.contractor_account(id),

    CONSTRAINT fk_appointment_client
        FOREIGN KEY (client_id)
        REFERENCES presta.client_account(id),

    CONSTRAINT check_appointment_duration
        CHECK (duration > 0 AND duration <= 480),

    CONSTRAINT unique_contractor_datetime
        UNIQUE (contractor_id, appointment_datetime)
);



CREATE INDEX idx_appointment_contractor ON presta.appointment(contractor_id);
CREATE INDEX idx_appointment_client ON presta.appointment(client_id);
CREATE INDEX idx_appointment_datetime ON presta.appointment(appointment_datetime);
CREATE INDEX idx_appointment_status ON presta.appointment(status);
