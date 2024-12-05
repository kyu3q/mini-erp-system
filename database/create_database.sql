-- データベースの作成
CREATE DATABASE mini_erp_db;

-- ユーザーの作成と権限付与
CREATE USER mini_erp_user WITH PASSWORD 'mini_erp_pass';
GRANT ALL PRIVILEGES ON DATABASE mini_erp_db TO mini_erp_user;

-- mini_erp_dbに接続
\c mini_erp_db

-- 製品テーブル
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted BOOLEAN DEFAULT FALSE,
    version BIGINT,
    product_code VARCHAR(50) UNIQUE NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    description TEXT,
    unit VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    minimum_stock INTEGER,
    maximum_stock INTEGER,
    reorder_point INTEGER
);

-- 倉庫テーブル
CREATE TABLE warehouses (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted BOOLEAN DEFAULT FALSE,
    version BIGINT,
    warehouse_code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(200),
    capacity INTEGER,
    status VARCHAR(20) NOT NULL,
    description TEXT
);

-- 在庫テーブル
CREATE TABLE stocks (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted BOOLEAN DEFAULT FALSE,
    version BIGINT,
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL,
    minimum_quantity INTEGER,
    maximum_quantity INTEGER,
    location VARCHAR(100),
    notes TEXT,
    UNIQUE(warehouse_id, product_id)
);

-- 製品の初期データ
INSERT INTO products (
    created_at, product_code, product_name, description, unit, status, 
    minimum_stock, maximum_stock, reorder_point
) VALUES
    (CURRENT_TIMESTAMP, 'P001', 'ノートパソコン', '高性能ビジネスノートPC', '台', 'ACTIVE', 10, 100, 20),
    (CURRENT_TIMESTAMP, 'P002', 'デスクトップPC', 'オフィス用デスクトップPC', '台', 'ACTIVE', 5, 50, 10),
    (CURRENT_TIMESTAMP, 'P003', 'USBメモリ 32GB', '高速USBメモリ', '個', 'ACTIVE', 50, 500, 100),
    (CURRENT_TIMESTAMP, 'P004', 'ワイヤレスマウス', 'エルゴノミクスデザイン', '個', 'ACTIVE', 20, 200, 50),
    (CURRENT_TIMESTAMP, 'P005', 'モニター27インチ', '4K対応ディスプレイ', '台', 'ACTIVE', 10, 80, 20);

-- 倉庫の初期データ
INSERT INTO warehouses (
    created_at, warehouse_code, name, address, capacity, status, description
) VALUES
    (CURRENT_TIMESTAMP, 'W001', '東京メイン倉庫', '東京都江東区豊洲1-1-1', 1000, 'ACTIVE', '主要保管施設'),
    (CURRENT_TIMESTAMP, 'W002', '大阪支店倉庫', '大阪府大阪市北区梅田2-2-2', 500, 'ACTIVE', '関西地域の配送拠点'),
    (CURRENT_TIMESTAMP, 'W003', '福岡物流センター', '福岡県福岡市博多区博多駅3-3-3', 300, 'ACTIVE', '九州地域の物流拠点');

-- 在庫の初期データ
INSERT INTO stocks (
    created_at, warehouse_id, product_id, quantity, minimum_quantity, maximum_quantity, location, notes
) VALUES
    (CURRENT_TIMESTAMP, 1, 1, 50, 10, 100, 'A-1-1', 'ノートPC保管エリア'),
    (CURRENT_TIMESTAMP, 1, 2, 30, 5, 50, 'A-1-2', 'デスクトップPC保管エリア'),
    (CURRENT_TIMESTAMP, 1, 3, 200, 50, 500, 'B-1-1', 'USBメモリ保管棚'),
    (CURRENT_TIMESTAMP, 2, 1, 25, 5, 50, 'A-1-1', '大阪ノートPC在庫'),
    (CURRENT_TIMESTAMP, 2, 4, 100, 20, 200, 'B-2-1', 'マウス保管エリア'),
    (CURRENT_TIMESTAMP, 3, 5, 40, 10, 80, 'C-1-1', 'モニター保管エリア');

-- mini_erp_userに必要な権限を付与
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO mini_erp_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO mini_erp_user;