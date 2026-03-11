-- ================================================================
--  Digital Banking Platform — Authentication Module
--  File   : auth-module-schema.sql
--  DB     : MySQL 8.x  |  PostgreSQL notes at bottom
--  Date   : 2026-03-11
-- ================================================================
--  Creation order (FK dependency):
--  roles → users → profiles → user_roles → verification_tokens → password_reset_tokens → activity_log
-- ================================================================

-- CREATE DATABASE banking_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE banking_db;

-- ----------------------------------------------------------------
-- 1. ROLES
-- ----------------------------------------------------------------
CREATE TABLE roles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name   VARCHAR(50)  NOT NULL UNIQUE,  -- ADMIN | CUSTOMER
    description VARCHAR(255),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------
-- 2. USERS  (auth credentials only — personal data is in profiles)
-- ----------------------------------------------------------------
CREATE TABLE users (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    email                 VARCHAR(150) NOT NULL UNIQUE,
    password              VARCHAR(255) NOT NULL,       -- BCrypt hash
    is_active             BOOLEAN      NOT NULL DEFAULT FALSE,  -- TRUE only after email + phone verified
    is_locked             BOOLEAN      NOT NULL DEFAULT FALSE,
    failed_login_attempts INT          NOT NULL DEFAULT 0,
    lock_time             TIMESTAMP    NULL,
    last_login_at         TIMESTAMP    NULL,
    email_verified        BOOLEAN      NOT NULL DEFAULT FALSE,
    phone_verified        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email)
);

-- ----------------------------------------------------------------
-- 3. PROFILES  (1:1 with users — KYC / personal details)
-- ----------------------------------------------------------------
CREATE TABLE profiles (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT       NOT NULL UNIQUE,
    full_name     VARCHAR(100) NOT NULL,
    phone_number  VARCHAR(15)  NOT NULL UNIQUE,
    date_of_birth DATE         NOT NULL,
    gender        ENUM('MALE','FEMALE','OTHER'),
    address       TEXT,
    city          VARCHAR(100),
    state         VARCHAR(100),
    pincode       VARCHAR(10),
    profile_pic   VARCHAR(500),
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_profiles_phone (phone_number)
);

-- ----------------------------------------------------------------
-- 4. USER_ROLES  (junction — supports multi-role in future)
-- ----------------------------------------------------------------
CREATE TABLE user_roles (
    user_id     BIGINT    NOT NULL,
    role_id     BIGINT    NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------
-- 5. VERIFICATION_TOKENS  (email link + mobile OTP, 15-min TTL)
-- ----------------------------------------------------------------
CREATE TABLE verification_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    token       VARCHAR(255) NOT NULL,                           -- UUID (email link) | 6-digit numeric OTP (mobile)
    type        ENUM('EMAIL','MOBILE') NOT NULL,
    expiry_date TIMESTAMP   NOT NULL,                           -- NOW() + 15 min
    is_used     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_vt_token (token),
    INDEX idx_vt_user_type (user_id, type)
);

-- ----------------------------------------------------------------
-- 6. PASSWORD_RESET_TOKENS  (single-use, 30-min TTL)
-- ----------------------------------------------------------------
CREATE TABLE password_reset_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    token       VARCHAR(255) NOT NULL UNIQUE,  -- UUID
    expiry_date TIMESTAMP    NOT NULL,         -- NOW() + 30 min
    is_used     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_prt_token (token)
);

-- ----------------------------------------------------------------
-- 7. ACTIVITY_LOG  (immutable audit trail — retain 90 days min)
-- ----------------------------------------------------------------
CREATE TABLE activity_log (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT NOT NULL,
    activity_type ENUM('LOGIN','LOGOUT','REGISTER',
                       'EMAIL_VERIFICATION_SENT','EMAIL_VERIFIED',
                       'MOBILE_OTP_SENT','MOBILE_VERIFIED',
                       'PASSWORD_RESET_REQUEST','PASSWORD_RESET','PASSWORD_CHANGE',
                       'ACCOUNT_LOCKED','ACCOUNT_UNLOCKED','TOKEN_REFRESH') NOT NULL,
    status        ENUM('SUCCESS','FAILURE') NOT NULL,
    ip_address    VARCHAR(50),
    user_agent    VARCHAR(500),
    description   VARCHAR(500),
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_al_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_al_user       (user_id),
    INDEX idx_al_created_at (created_at)
);

-- ================================================================
-- SEED DATA
-- ================================================================

INSERT INTO roles (role_name, description) VALUES
    ('ADMIN',    'Platform administrator'),
    ('CUSTOMER', 'Self-registered bank customer');

-- IMPORTANT: Replace the password hash before deploying.
-- Generate: new BCryptPasswordEncoder(12).encode("Admin@1234")
INSERT INTO users (email, password, is_active, is_locked, email_verified, phone_verified)
VALUES ('admin@bankplatform.com',
        '$2a$12$REPLACE_THIS_HASH_BEFORE_DEPLOYING',
        TRUE, FALSE, TRUE, TRUE);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.role_name = 'ADMIN'
WHERE u.email = 'admin@bankplatform.com';

INSERT INTO profiles (user_id, full_name, phone_number, date_of_birth)
SELECT id, 'System Administrator', '0000000000', '1990-01-01'
FROM users WHERE email = 'admin@bankplatform.com';

-- ================================================================
-- DEV / DEBUG QUERIES
-- ================================================================

-- All users with roles:
-- SELECT u.id, u.email, r.role_name, u.is_active, u.is_locked, u.last_login_at
-- FROM users u JOIN user_roles ur ON ur.user_id = u.id JOIN roles r ON r.id = ur.role_id;

-- Recent activity for a user:
-- SELECT activity_type, status, ip_address, created_at FROM activity_log
-- WHERE user_id = ? ORDER BY created_at DESC LIMIT 50;

-- Valid unused reset tokens:
-- SELECT prt.token, prt.expiry_date, u.email FROM password_reset_tokens prt
-- JOIN users u ON u.id = prt.user_id WHERE prt.is_used = FALSE AND prt.expiry_date > NOW();

-- Pending verifications (users not yet fully active):
-- SELECT u.email, u.email_verified, u.phone_verified FROM users u
-- WHERE u.is_active = FALSE AND u.is_locked = FALSE;

-- ================================================================
-- POSTGRESQL MIGRATION NOTES
-- ================================================================
-- 1. AUTO_INCREMENT  →  BIGSERIAL  (or GENERATED ALWAYS AS IDENTITY)
-- 2. ENUM inline  →  CREATE TYPE first, then use the type name
--      CREATE TYPE activity_type_enum AS ENUM ('LOGIN','LOGOUT',...);
--      CREATE TYPE status_enum AS ENUM ('SUCCESS','FAILURE');
--      CREATE TYPE gender_enum AS ENUM ('MALE','FEMALE','OTHER');
-- 3. ON UPDATE CURRENT_TIMESTAMP  →  use a trigger:
--      CREATE OR REPLACE FUNCTION fn_updated_at() RETURNS TRIGGER AS $$
--      BEGIN NEW.updated_at = NOW(); RETURN NEW; END; $$ LANGUAGE plpgsql;
--      CREATE TRIGGER trg_users_upd BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION fn_updated_at();
--      CREATE TRIGGER trg_profiles_upd BEFORE UPDATE ON profiles FOR EACH ROW EXECUTE FUNCTION fn_updated_at();
-- 4. Move INDEX declarations outside CREATE TABLE:
--      CREATE INDEX idx_users_email    ON users(email);
--      CREATE INDEX idx_profiles_phone ON profiles(phone_number);
--      CREATE INDEX idx_prt_token      ON password_reset_tokens(token);
--      CREATE INDEX idx_al_user        ON activity_log(user_id);
--      CREATE INDEX idx_al_created_at  ON activity_log(created_at);
-- ================================================================

