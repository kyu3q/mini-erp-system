-- データベースの作成（存在しない場合のみ）
DO
$$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'mini_erp_db') THEN
        CREATE DATABASE mini_erp_db;
    END IF;
END
$$;

-- ユーザーの作成（存在しない場合のみ）と権限付与
DO
$$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'mini_erp_user') THEN
        CREATE USER mini_erp_user WITH PASSWORD 'mini_erp_pass';
    END IF;
END
$$;

GRANT ALL PRIVILEGES ON DATABASE mini_erp_db TO mini_erp_user;
