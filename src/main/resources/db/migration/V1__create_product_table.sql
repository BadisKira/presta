CREATE TABLE PRODUCT
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255)   NOT NULL,
    description VARCHAR(255),
    price       DECIMAL(10, 2) NOT NULL,
    created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);
