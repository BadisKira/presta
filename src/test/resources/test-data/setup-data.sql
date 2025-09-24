-- ============================================
-- Nettoyage des données (dans l'ordre des FK)
-- ============================================
DELETE FROM presta.appointment;
DELETE FROM presta.break_time;
DELETE FROM presta.unavailability_rule;
DELETE FROM presta.availability_rule;
DELETE FROM presta.contractor_account;
DELETE FROM presta.client_account;
DELETE FROM presta.assignment;
DELETE FROM presta.user_app;

-- ============================================
-- 1. Table assignment (2 entrées)
-- ============================================
INSERT INTO presta.assignment (id, name, description)
VALUES
    ('a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d', 'Plomberie', 'Services de plomberie générale et dépannage'),
    ('b2c3d4e5-f6a7-5b6c-9d0e-1f2a3b4c5d6e', 'Électricité', 'Installation et maintenance électrique')
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- 2. Table user_app (4 entrées: 2 contractors + 2 clients)
-- ============================================
-- Users pour contractors
INSERT INTO presta.user_app (id, keycloak_id, first_name, last_name, email, is_active)
VALUES
    ('41e9240a-d656-435f-80d3-731ec3c3f501', '11111111-1111-1111-1111-111111111111', 'Jean', 'Dupont', 'jean.dupont@contractor.com', true),
    ('52fa351b-e767-546f-91e4-842fd4d47602', '22222222-2222-2222-2222-222222222222', 'Marie', 'Durand', 'marie.durand@contractor.com', true)
ON CONFLICT (id) DO NOTHING;

-- Users pour clients
INSERT INTO presta.user_app (id, keycloak_id, first_name, last_name, email, is_active)
VALUES
    ('7b3e9f12-8a45-4c3d-9e1f-5d8c2a1b6f90', '33333333-3333-3333-3333-333333333333', 'Paul', 'Martin', 'paul.martin@client.com', true),
    ('8c4faf23-9b56-5d4e-0f22-6e9d3b2c7aa1', '44444444-4444-4444-4444-444444444444', 'Sophie', 'Bernard', 'sophie.bernard@client.com', true)
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- 3. Table contractor_account (2 entrées)
-- ============================================
INSERT INTO presta.contractor_account (id, full_name, address, assignment_id, speciality)
VALUES
    ('41e9240a-d656-435f-80d3-731ec3c3f501', 'Jean Dupont', '15 Rue de la République, 75001 Paris', 'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d', 'Plomberie sanitaire'),
    ('52fa351b-e767-546f-91e4-842fd4d47602', 'Marie Durand', '28 Avenue des Champs, 69000 Lyon', 'b2c3d4e5-f6a7-5b6c-9d0e-1f2a3b4c5d6e', 'Installation électrique')
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- 4. Table client_account (2 entrées)
-- ============================================
INSERT INTO presta.client_account (id)
VALUES
    ('7b3e9f12-8a45-4c3d-9e1f-5d8c2a1b6f90'),
    ('8c4faf23-9b56-5d4e-0f22-6e9d3b2c7aa1')
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- 5. Table availability_rule (2 entrées)
-- ============================================
-- Règle 1: Jean Dupont - Lundi au Vendredi, 9h-17h, créneaux de 30min
INSERT INTO presta.availability_rule (id, contractor_id, week_days, start_time, end_time, slot_duration, rest_time, is_active, created_at, updated_at)
VALUES
    ('d1e2f3a4-b5c6-7d8e-9f0a-1b2c3d4e5f6a',
     '41e9240a-d656-435f-80d3-731ec3c3f501',
     ARRAY[1,2,3,4,5],
     '09:00:00',
     '17:00:00',
     30,
     0,
     true,
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Règle 2: Marie Durand - Mardi, Jeudi, Samedi, 10h-18h, créneaux de 45min
INSERT INTO presta.availability_rule (id, contractor_id, week_days, start_time, end_time, slot_duration, rest_time, is_active, created_at, updated_at)
VALUES
    ('e2f3a4b5-c6d7-8e9f-0a1b-2c3d4e5f6a7b',
     '52fa351b-e767-546f-91e4-842fd4d47602',
     ARRAY[2,4,6],
     '10:00:00',
     '18:00:00',
     45,
     10,
     true,
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- 6. Table break_time (2 entrées - optionnelles)
-- ============================================
-- Pause déjeuner pour Jean Dupont (12h-13h tous les jours de sa règle)
INSERT INTO presta.break_time (id, availability_rule_id, start_time, end_time, week_days)
VALUES
    ('f3a4b5c6-d7e8-9f0a-1b2c-3d4e5f6a7b8c',
     'd1e2f3a4-b5c6-7d8e-9f0a-1b2c3d4e5f6a',
     '12:00:00',
     '13:00:00',
     NULL)
ON CONFLICT (id) DO NOTHING;

-- Pause café pour Marie Durand (15h-15h15 seulement le mardi et jeudi)
INSERT INTO presta.break_time (id, availability_rule_id, start_time, end_time, week_days)
VALUES
    ('a4b5c6d7-e8f9-0a1b-2c3d-4e5f6a7b8c9d',
     'e2f3a4b5-c6d7-8e9f-0a1b-2c3d4e5f6a7b',
     '15:00:00',
     '15:15:00',
     ARRAY[2,4])
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- 7. Table unavailability_rule (2 entrées)
-- ============================================
-- Jean Dupont en congé du 15 au 20 décembre 2025
INSERT INTO presta.unavailability_rule (id, contractor_id, start_date, end_date, start_time, end_time, reason, created_at)
VALUES
    ('b5c6d7e8-f9a0-1b2c-3d4e-5f6a7b8c9d0e',
     '41e9240a-d656-435f-80d3-731ec3c3f501',
     '2025-12-15',
     '2025-12-20',
     NULL,
     NULL,
     'Congés de fin d''année',
     CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Marie Durand indisponible le 10 novembre 2025 de 14h à 16h
INSERT INTO presta.unavailability_rule (id, contractor_id, start_date, end_date, start_time, end_time, reason, created_at)
VALUES
    ('c6d7e8f9-a0b1-2c3d-4e5f-6a7b8c9d0e1f',
     '52fa351b-e767-546f-91e4-842fd4d47602',
     '2025-11-10',
     '2025-11-10',
     '14:00:00',
     '16:00:00',
     'Rendez-vous médical',
     CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- 8. Table appointment (2 entrées)
-- ============================================
-- RDV 1: Paul Martin avec Jean Dupont le 7 octobre 2025 à 10h
INSERT INTO presta.appointment (id, contractor_id, client_id, appointment_datetime, duration, status, reason, notes, created_at, updated_at)
VALUES
    ('d7e8f9a0-b1c2-3d4e-5f6a-7b8c9d0e1f2a',
     '41e9240a-d656-435f-80d3-731ec3c3f501',
     '7b3e9f12-8a45-4c3d-9e1f-5d8c2a1b6f90',
     '2025-10-07 10:00:00',
     30,
     'CONFIRMED',
     'Réparation fuite évier',
     'Client disponible toute la matinée',
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP)
ON CONFLICT (contractor_id, appointment_datetime) DO NOTHING;

-- RDV 2: Sophie Bernard avec Marie Durand le 8 octobre 2025 à 14h
INSERT INTO presta.appointment (id, contractor_id, client_id, appointment_datetime, duration, status, reason, notes, created_at, updated_at)
VALUES
    ('e8f9a0b1-c2d3-4e5f-6a7b-8c9d0e1f2a3b',
     '52fa351b-e767-546f-91e4-842fd4d47602',
     '8c4faf23-9b56-5d4e-0f22-6e9d3b2c7aa1',
     '2025-10-08 14:00:00',
     45,
     'PENDING',
     'Installation prises électriques',
     'Appartement au 3ème étage',
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP)
ON CONFLICT (contractor_id, appointment_datetime) DO NOTHING;