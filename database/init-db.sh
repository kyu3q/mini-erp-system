#!/bin/bash
set -e

# Create database and user
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -f /docker-entrypoint-initdb.d/create_database.sql

# Switch to mini_erp_db and create schema objects
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "mini_erp_db" -f /docker-entrypoint-initdb.d/create_tables.sql
