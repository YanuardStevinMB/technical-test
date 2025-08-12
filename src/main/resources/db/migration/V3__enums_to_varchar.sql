-- V3: Convert PostgreSQL ENUM columns to VARCHAR for JPA compatibility (EnumType.STRING)
-- Reason: Error "column is of type <enum> but expression is of type character varying"
-- Strategy: Cast the enum value to text and change the column type to VARCHAR(20)

-- accounts
ALTER TABLE accounts
ALTER COLUMN status TYPE VARCHAR(20) USING status::text,
    ALTER COLUMN account_type TYPE VARCHAR(20) USING account_type::text;

-- transactions
ALTER TABLE transactions
ALTER COLUMN type TYPE VARCHAR(20) USING type::text,
    ALTER COLUMN status TYPE VARCHAR(20) USING status::text;

-- movements
ALTER TABLE movements
ALTER COLUMN movement_type TYPE VARCHAR(20) USING movement_type::text;
