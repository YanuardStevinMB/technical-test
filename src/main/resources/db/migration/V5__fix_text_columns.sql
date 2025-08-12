-- V5: Fix text columns in clients if they were created as BYTEA by mistake
DO $$
BEGIN
    -- first_name
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'clients'
          AND column_name = 'first_name'
          AND data_type = 'bytea'
    ) THEN
        EXECUTE 'ALTER TABLE clients ALTER COLUMN first_name TYPE VARCHAR(100) USING convert_from(first_name, ''UTF8'')';
END IF;

    -- last_name
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'clients'
          AND column_name = 'last_name'
          AND data_type = 'bytea'
    ) THEN
        EXECUTE 'ALTER TABLE clients ALTER COLUMN last_name TYPE VARCHAR(100) USING convert_from(last_name, ''UTF8'')';
END IF;

    -- email
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'clients'
          AND column_name = 'email'
          AND data_type = 'bytea'
    ) THEN
        EXECUTE 'ALTER TABLE clients ALTER COLUMN email TYPE VARCHAR(255) USING convert_from(email, ''UTF8'')';
END IF;
END$$;
