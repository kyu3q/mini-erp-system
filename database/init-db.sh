#!/bin/bash
set -e

# Create database and user
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -f /docker-entrypoint-initdb.d/create_database.sql

# Update pg_hba.conf to allow password authentication
echo "host    mini_erp_db    mini_erp_user    all    md5" >> "$PGDATA/pg_hba.conf"

# Reload PostgreSQL configuration
pg_ctl reload

# Switch to mini_erp_db and create schema objects
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "mini_erp_db" -f /docker-entrypoint-initdb.d/create_tables.sql
