-- Flyway V4: Add unique constraints and additional validations

-- =========================
-- CLIENTS TABLE CONSTRAINTS
-- =========================


-- Add composite unique constraint for identification type + number
ALTER TABLE clients
    ADD CONSTRAINT uk_client_identification_type_number
        UNIQUE (identification_type, identification_number);

-- Remove simple unique constraint on identification_number (now part of composite)
ALTER TABLE clients
DROP CONSTRAINT IF EXISTS clients_identification_number_key;

-- Ensure first and last names have at least 2 characters
ALTER TABLE clients
    ADD CONSTRAINT chk_first_name_min_length
        CHECK (LENGTH(TRIM(first_name)) >= 2);

ALTER TABLE clients
    ADD CONSTRAINT chk_last_name_min_length
        CHECK (LENGTH(TRIM(last_name)) >= 2);

-- Validate email format if not null
ALTER TABLE clients
    ADD CONSTRAINT chk_email_format
        CHECK (
            email IS NULL
                OR email ~ '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
    );

-- Ensure client is at least 18 years old
ALTER TABLE clients
    ADD CONSTRAINT chk_client_minimum_age
        CHECK (birth_date <= CURRENT_DATE - INTERVAL '18 years');

-- =========================
-- ACCOUNTS TABLE CONSTRAINTS
-- =========================

-- Savings account balance cannot be negative
ALTER TABLE accounts
    ADD CONSTRAINT chk_savings_balance_non_negative
        CHECK (NOT (account_type = 'AHORROS' AND balance < 0));

-- Validate account number format
ALTER TABLE accounts
    ADD CONSTRAINT chk_account_number_format
        CHECK (
            (account_type = 'AHORROS' AND account_number ~ '^53[0-9]{8}$') OR
            (account_type = 'CORRIENTE' AND account_number ~ '^33[0-9]{8}$')
            );

-- =========================
-- TRANSACTIONS TABLE CONSTRAINTS
-- =========================

-- Ensure transaction amount is positive
ALTER TABLE transactions
    ADD CONSTRAINT chk_transaction_amount_positive
        CHECK (amount > 0);

-- Ensure at least one account is present
ALTER TABLE transactions
    ADD CONSTRAINT chk_transaction_account_present
        CHECK (source_account_id IS NOT NULL OR destination_account_id IS NOT NULL);

-- Ensure origin and destination accounts are different for transfers
ALTER TABLE transactions
    ADD CONSTRAINT chk_origin_destination_different
        CHECK (
            type != 'TRANSFERENCIA' OR
    (source_account_id IS NOT NULL
    AND destination_account_id IS NOT NULL
    AND source_account_id != destination_account_id)
    );

-- =========================
-- MOVEMENTS TABLE CONSTRAINTS
-- =========================

-- Ensure movement amount is positive
ALTER TABLE movements
    ADD CONSTRAINT chk_movement_amount_positive
        CHECK (amount > 0);

-- =========================
-- PERFORMANCE INDEXES
-- =========================

CREATE INDEX IF NOT EXISTS idx_clients_identification_type_number
    ON clients(identification_type, identification_number);

CREATE INDEX IF NOT EXISTS idx_accounts_status
    ON accounts(status);

CREATE INDEX IF NOT EXISTS idx_transactions_type_date
    ON transactions(type, date);
