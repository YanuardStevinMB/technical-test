-- V2: Adjust account_number column type to VARCHAR(10)
-- Reason: Hibernate validates VARCHAR(10) but the initial schema created CHAR(10)
-- Note: TRIM is used to remove possible spaces in existing values

ALTER TABLE accounts
ALTER COLUMN account_number TYPE VARCHAR(10) USING TRIM(account_number);
