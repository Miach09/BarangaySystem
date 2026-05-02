-- ============================================================
--  Barangay Bagong Sikat – Database Setup Script v2
--  Run this in phpMyAdmin > SQL tab
-- ============================================================

CREATE DATABASE IF NOT EXISTS barangay_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE barangay_db;

-- ── Users table ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(150)        NOT NULL,
    email       VARCHAR(150)        NOT NULL UNIQUE,
    phone       VARCHAR(20)         NOT NULL,
    address     VARCHAR(255)        NOT NULL DEFAULT '',
    password    VARCHAR(255)        NOT NULL,
    is_admin    TINYINT(1)          NOT NULL DEFAULT 0,
    created_at  DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ── Requests table ───────────────────────────────────────────
CREATE TABLE IF NOT EXISTS requests (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    requester_name  VARCHAR(150)    NOT NULL,
    contact_number  VARCHAR(20)     NOT NULL,
    email           VARCHAR(150)    NOT NULL,
    request_type    VARCHAR(100)    NOT NULL,
    description     TEXT            NOT NULL,
    status          ENUM('PENDING','RESOLVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    reject_reason   TEXT            DEFAULT NULL,
    submitted_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ── If upgrading from v1, run these ALTER statements ─────────
-- ALTER TABLE users ADD COLUMN address VARCHAR(255) NOT NULL DEFAULT '' AFTER phone;
-- ALTER TABLE requests ADD COLUMN reject_reason TEXT DEFAULT NULL AFTER status;
-- ALTER TABLE requests MODIFY COLUMN status ENUM('PENDING','RESOLVED','REJECTED') NOT NULL DEFAULT 'PENDING';

-- ── Seed data ─────────────────────────────────────────────────
INSERT INTO users (full_name, email, phone, address, password, is_admin) VALUES
    ('Admin User',   'admin@barangay.gov.ph', '+63 900 000 0001', 'Barangay Hall, Bagong Sikat', 'admin123', 1),
    ('Maria Santos', 'maria@gmail.com',        '+63 912 345 6789', '123 Rizal St, Bagong Sikat',  'pass123',  0);

INSERT INTO requests (requester_name, contact_number, email, request_type, description, status) VALUES
    ('Maria Santos', '+63 912 345 6789', 'maria@gmail.com',
     'Barangay Clearance Application', 'Needed for employment requirements.', 'PENDING'),
    ('Maria Santos', '+63 912 345 6789', 'maria@gmail.com',
     'Certificate of Indigency', 'Needed for scholarship application.', 'PENDING'),
    ('Pedro Garcia',  '+63 917 111 2222', 'pedro@gmail.com',
     'Certificate of Residency', 'Required by the court.', 'RESOLVED');

-- ── Verify ────────────────────────────────────────────────────
SELECT 'users' AS table_name, COUNT(*) AS total FROM users
UNION ALL
SELECT 'requests' AS table_name, COUNT(*) AS total FROM requests;