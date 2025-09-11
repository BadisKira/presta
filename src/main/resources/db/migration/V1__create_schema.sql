CREATE TABLE presta.userApp (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    keycloak_id UUID UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL
);


CREATE TABLE presta.accountClient (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_app_id UUID UNIQUE NOT NULL,
    FOREIGN KEY (user_app_id) REFERENCES presta.userApp(id) ON DELETE CASCADE
);


CREATE TABLE presta.assignment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);


CREATE TABLE presta.contractorAccount (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_app_id UUID UNIQUE NOT NULL,
    full_name VARCHAR(150),
    address TEXT,
    assignment_id UUID,
    speciality VARCHAR(100),
    FOREIGN KEY (user_app_id) REFERENCES presta.userApp(id) ON DELETE CASCADE,
    FOREIGN KEY (assignment_id) REFERENCES presta.assignment(id) ON DELETE SET NULL
);

