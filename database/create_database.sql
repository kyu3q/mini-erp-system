-- データベースの作成
CREATE DATABASE IF NOT EXISTS mini_erp_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- データベースユーザーの作成
CREATE USER IF NOT EXISTS 'mini_erp_user'@'localhost' IDENTIFIED BY 'mini_erp_pass';
CREATE USER IF NOT EXISTS 'mini_erp_user'@'%' IDENTIFIED BY 'mini_erp_pass';

-- 権限の付与
GRANT ALL PRIVILEGES ON mini_erp_db.* TO 'mini_erp_user'@'localhost';
GRANT ALL PRIVILEGES ON mini_erp_db.* TO 'mini_erp_user'@'%';
FLUSH PRIVILEGES;