-- データベースの作成（存在しない場合のみ）
SELECT 'CREATE DATABASE mini_erp_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'mini_erp_db')\gexec

-- ユーザーの作成（存在しない場合のみ）と権限付与
DO
$$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'mini_erp_user') THEN
        CREATE USER mini_erp_user WITH PASSWORD 'mini_erp_pass' LOGIN;
    END IF;
END
$$;

-- データベースの所有者を変更
ALTER DATABASE mini_erp_db OWNER TO mini_erp_user;

-- スキーマの権限を付与
\c mini_erp_db
GRANT ALL PRIVILEGES ON SCHEMA public TO mini_erp_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO mini_erp_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO mini_erp_user;
