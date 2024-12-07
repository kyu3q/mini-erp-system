-- データベースの作成
CREATE DATABASE mini_erp_db;

-- ユーザーの作成と権限付与
CREATE USER mini_erp_user WITH PASSWORD 'mini_erp_pass';
GRANT ALL PRIVILEGES ON DATABASE mini_erp_db TO mini_erp_user;
