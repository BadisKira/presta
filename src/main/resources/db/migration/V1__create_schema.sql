CREATE TABLE presta.user_app (
    id UUID PRIMARY KEY  ,
    keycloak_id UUID UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE presta.client_account (
    id UUID PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES presta.user_app(id) ON DELETE CASCADE
);

CREATE TABLE presta.assignment (
    id UUID PRIMARY KEY ,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

-- Table Contractor avec même ID que User
CREATE TABLE presta.contractor_account (
    id UUID PRIMARY KEY,
    full_name VARCHAR(150),
    address TEXT,
    assignment_id UUID,
    speciality VARCHAR(100),
    FOREIGN KEY (id) REFERENCES presta.user_app(id) ON DELETE CASCADE,
    FOREIGN KEY (assignment_id) REFERENCES presta.assignment(id) ON DELETE SET NULL
);