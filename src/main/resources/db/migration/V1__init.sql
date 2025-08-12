-- Flyway V1: Initial schema for clients, accounts, transactions, movements

-- ========================
-- 1. Enums
-- ========================
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'account_type') THEN
CREATE TYPE account_type AS ENUM ('AHORROS', 'CORRIENTE');
END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'account_status') THEN
CREATE TYPE account_status AS ENUM ('ACTIVE', 'INACTIVE', 'CANCELED');
END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'transaction_type') THEN
CREATE TYPE transaction_type AS ENUM ('CONSIGNACION', 'RETIRO', 'TRANSFERENCIA');
END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'transaction_status') THEN
CREATE TYPE transaction_status AS ENUM ('OK', 'FAILED');
END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'movement_type') THEN
CREATE TYPE movement_type AS ENUM ('DEBIT', 'CREDIT');
END IF;
END $$;

-- ========================
-- 2. Sequence
-- ========================
CREATE SEQUENCE IF NOT EXISTS seq_accounts START 1 INCREMENT 1;

-- ========================
-- 3. Tables
-- ========================

-- Clients
CREATE TABLE IF NOT EXISTS clients (
                                       id UUID PRIMARY KEY,
                                       identification_type VARCHAR(10) NOT NULL,
    identification_number VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    birth_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0
    );

-- Accounts
CREATE TABLE IF NOT EXISTS accounts (
                                        id UUID PRIMARY KEY,
                                        client_id UUID NOT NULL REFERENCES clients(id),
    account_type account_type NOT NULL,
    account_number CHAR(10) NOT NULL UNIQUE,
    status account_status NOT NULL,
    balance NUMERIC(18,2) NOT NULL,
    gmf_exempt BOOLEAN NOT NULL DEFAULT FALSE,
    owner_user VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0
    );
CREATE INDEX IF NOT EXISTS idx_accounts_client ON accounts(client_id);

-- Transactions
CREATE TABLE IF NOT EXISTS transactions (
                                            id UUID PRIMARY KEY,
                                            type transaction_type NOT NULL,
                                            date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    source_account_id UUID NULL REFERENCES accounts(id),
    destination_account_id UUID NULL REFERENCES accounts(id),
    amount NUMERIC(18,2) NOT NULL,
    description VARCHAR(255),
    reference VARCHAR(100),
    status transaction_status NOT NULL,
    created_by VARCHAR(100)
    );
CREATE INDEX IF NOT EXISTS idx_transactions_source_account ON transactions(source_account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_destination_account ON transactions(destination_account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(date);

-- Movements
CREATE TABLE IF NOT EXISTS movements (
                                         id UUID PRIMARY KEY,
                                         transaction_id UUID NOT NULL REFERENCES transactions(id),
    account_id UUID NOT NULL REFERENCES accounts(id),
    movement_type movement_type NOT NULL,
    amount NUMERIC(18,2) NOT NULL,
    balance_before NUMERIC(18,2) NOT NULL,
    balance_after NUMERIC(18,2) NOT NULL,
    date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
    );
CREATE INDEX IF NOT EXISTS idx_movements_account ON movements(account_id);
CREATE INDEX IF NOT EXISTS idx_movements_transaction ON movements(transaction_id);


