-- Enable the pg_trgm extension for similarity searches
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Create the database schema, etc. - application tables will be created with Flyway/Liquibase
