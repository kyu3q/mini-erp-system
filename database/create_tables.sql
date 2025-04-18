\c mini_erp_db;

-- スキーマの権限付与
GRANT ALL ON SCHEMA public TO mini_erp_user;

-- 品目テーブル
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'items') THEN
        CREATE TABLE items (
            id BIGSERIAL PRIMARY KEY,
            created_at TIMESTAMP NOT NULL,
            updated_at TIMESTAMP,
            created_by VARCHAR(100),
            updated_by VARCHAR(100),
            deleted BOOLEAN DEFAULT FALSE,
            item_code VARCHAR(50) NOT NULL,
            item_name VARCHAR(100) NOT NULL,
            description TEXT,
            unit VARCHAR(20) NOT NULL,
            status VARCHAR(20) NOT NULL,
            minimum_stock INTEGER,
            maximum_stock INTEGER,
            reorder_point INTEGER,
            CONSTRAINT uk_items_item_code_not_deleted UNIQUE (item_code, deleted)
        );
    END IF;
END $$;

-- 倉庫テーブル
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'warehouses') THEN
        CREATE TABLE warehouses (
            id BIGSERIAL PRIMARY KEY,
            created_at TIMESTAMP NOT NULL,
            updated_at TIMESTAMP,
            created_by VARCHAR(100),
            updated_by VARCHAR(100),
            deleted BOOLEAN DEFAULT FALSE,
            warehouse_code VARCHAR(50) NOT NULL,
            name VARCHAR(100) NOT NULL,
            address VARCHAR(200),
            capacity INTEGER,
            status VARCHAR(20) NOT NULL,
            description TEXT,
            CONSTRAINT uk_warehouses_warehouse_code_not_deleted UNIQUE (warehouse_code, deleted)
        );
    END IF;
END $$;

-- 得意先テーブル
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'customers') THEN
        CREATE TABLE customers (
            id BIGSERIAL PRIMARY KEY,
            created_at TIMESTAMP NOT NULL,
            updated_at TIMESTAMP,
            created_by VARCHAR(100),
            updated_by VARCHAR(100),
            deleted BOOLEAN DEFAULT FALSE,
            customer_code VARCHAR(50) NOT NULL,
            name VARCHAR(100) NOT NULL,
            name_kana VARCHAR(100),
            postal_code VARCHAR(8),
            address VARCHAR(200),
            phone VARCHAR(20),
            email VARCHAR(100),
            fax VARCHAR(20),
            contact_person VARCHAR(100),
            payment_terms VARCHAR(100),
            status VARCHAR(20) NOT NULL,
            notes TEXT,
            CONSTRAINT uk_customers_customer_code_not_deleted UNIQUE (customer_code, deleted)
        );
    END IF;
END $$;

-- 仕入先テーブル
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'suppliers') THEN
        CREATE TABLE suppliers (
            id BIGSERIAL PRIMARY KEY,
            created_at TIMESTAMP NOT NULL,
            updated_at TIMESTAMP,
            created_by VARCHAR(100),
            updated_by VARCHAR(100),
            deleted BOOLEAN DEFAULT FALSE,
            supplier_code VARCHAR(50) NOT NULL,
            name VARCHAR(100) NOT NULL,
            name_kana VARCHAR(100),
            postal_code VARCHAR(8),
            address VARCHAR(200),
            phone VARCHAR(20),
            email VARCHAR(100),
            fax VARCHAR(20),
            contact_person VARCHAR(100),
            payment_terms VARCHAR(100),
            status VARCHAR(20) NOT NULL,
            notes TEXT,
            CONSTRAINT uk_suppliers_supplier_code_not_deleted UNIQUE (supplier_code, deleted)
        );
    END IF;
END $$;

-- 初期データの挿入（テーブルが空の場合のみ）
DO $$
BEGIN
    -- 品目の初期データ
    IF NOT EXISTS (SELECT 1 FROM items) THEN
        INSERT INTO items (
            created_at, created_by, item_code, item_name, description, unit, status, 
            minimum_stock, maximum_stock, reorder_point
        ) VALUES
            (CURRENT_TIMESTAMP, 'system', 'P001', 'ノートパソコン', '高性能ビジネスノートPC', '台', 'ACTIVE', 10, 100, 20),
            (CURRENT_TIMESTAMP, 'system', 'P002', 'デスクトップPC', 'オフィス用デスクトップPC', '台', 'ACTIVE', 5, 50, 10),
            (CURRENT_TIMESTAMP, 'system', 'P003', 'USBメモリ 32GB', '高速USBメモリ', '個', 'ACTIVE', 50, 500, 100),
            (CURRENT_TIMESTAMP, 'system', 'P004', 'ワイヤレスマウス', 'エルゴノミクスデザイン', '個', 'ACTIVE', 20, 200, 50),
            (CURRENT_TIMESTAMP, 'system', 'P005', 'モニター27インチ', '4K対応ディスプレイ', '台', 'ACTIVE', 10, 80, 20);
    END IF;

    -- 倉庫の初期データ
    IF NOT EXISTS (SELECT 1 FROM warehouses) THEN
        INSERT INTO warehouses (
            created_at, created_by, warehouse_code, name, address, capacity, status, description
        ) VALUES
            (CURRENT_TIMESTAMP, 'system', 'W001', '東京メイン倉庫', '東京都江東区豊洲1-1-1', 1000, 'ACTIVE', '主要保管施設'),
            (CURRENT_TIMESTAMP, 'system', 'W002', '大阪支店倉庫', '大阪府大阪市北区梅田2-2-2', 500, 'ACTIVE', '関西地域の配送拠点'),
            (CURRENT_TIMESTAMP, 'system', 'W003', '福岡物流センター', '福岡県福岡市博多区博多駅3-3-3', 300, 'ACTIVE', '九州地域の物流拠点');
    END IF;

    -- 得意先の初期データ
    IF NOT EXISTS (SELECT 1 FROM customers) THEN
        INSERT INTO customers (
            created_at, created_by, customer_code, name, name_kana, postal_code, address,
            phone, email, status
        ) VALUES
            (CURRENT_TIMESTAMP, 'system', 'C001', '株式会社ABC商事', 'カブシキガイシャエービーシーショウジ', '100-0001', '東京都千代田区丸の内1-1-1',
             '03-1234-5678', 'contact@abc-trading.example.com', 'ACTIVE'),
            (CURRENT_TIMESTAMP, 'system', 'C002', '株式会社XYZ物産', 'カブシキガイシャエックスワイゼットブッサン', '541-0041', '大阪府大阪市中央区北浜2-2-2',
             '06-2345-6789', 'info@xyz-trading.example.com', 'ACTIVE');
    END IF;

    -- 仕入先の初期データ
    IF NOT EXISTS (SELECT 1 FROM suppliers) THEN
        INSERT INTO suppliers (
            created_at, created_by, supplier_code, name, name_kana, postal_code, address,
            phone, email, status
        ) VALUES
            (CURRENT_TIMESTAMP, 'system', 'S001', '株式会社テクノサプライ', 'カブシキガイシャテクノサプライ', '108-0075', '東京都港区港南3-3-3',
             '03-3456-7890', 'contact@techno-supply.example.com', 'ACTIVE'),
            (CURRENT_TIMESTAMP, 'system', 'S002', '株式会社グローバルパーツ', 'カブシキガイシャグローバルパーツ', '532-0003', '大阪府大阪市淀川区宮原4-4-4',
             '06-4567-8901', 'info@global-parts.example.com', 'ACTIVE');
    END IF;
END $$;

-- 受注テーブル
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'orders') THEN
        CREATE TABLE orders (
            id BIGSERIAL PRIMARY KEY,
            created_at TIMESTAMP NOT NULL,
            updated_at TIMESTAMP,
            created_by VARCHAR(100),
            updated_by VARCHAR(100),
            deleted BOOLEAN DEFAULT FALSE,
            order_number VARCHAR(50) NOT NULL,
            order_date DATE NOT NULL,
            customer_id BIGINT NOT NULL REFERENCES customers(id),
            delivery_date DATE,
            shipping_address VARCHAR(200),
            shipping_postal_code VARCHAR(8),
            shipping_phone VARCHAR(20),
            shipping_contact_person VARCHAR(100),
            status VARCHAR(20) NOT NULL,
            notes TEXT,
            total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
            tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
            CONSTRAINT uk_orders_order_number_not_deleted UNIQUE (order_number, deleted)
        );
    END IF;
END $$;

-- 受注明細テーブル
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'order_details') THEN
        CREATE TABLE order_details (
            id BIGSERIAL PRIMARY KEY,
            created_at TIMESTAMP NOT NULL,
            updated_at TIMESTAMP,
            created_by VARCHAR(100),
            updated_by VARCHAR(100),
            deleted BOOLEAN DEFAULT FALSE,
            order_id BIGINT NOT NULL REFERENCES orders(id),
            line_number INTEGER NOT NULL,
            item_id BIGINT REFERENCES items(id),
            item_name VARCHAR(100) NOT NULL,
            quantity INTEGER NOT NULL,
            unit_price DECIMAL(12,2) NOT NULL,
            amount DECIMAL(12,2) NOT NULL,
            warehouse_id BIGINT REFERENCES warehouses(id),
            delivery_date DATE,
            notes TEXT,
            CONSTRAINT uk_order_details_order_line UNIQUE(order_id, line_number, deleted)
        );
    END IF;
END $$;

-- 価格条件マスタ
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'prices') THEN
        CREATE TABLE prices (
            id BIGSERIAL PRIMARY KEY,
            created_at TIMESTAMP NOT NULL,
            updated_at TIMESTAMP,
            created_by VARCHAR(100),
            updated_by VARCHAR(100),
            deleted BOOLEAN DEFAULT FALSE,
            price_type VARCHAR(20) NOT NULL CHECK (price_type IN ('SALES', 'PURCHASE')),
            item_id BIGINT NOT NULL REFERENCES items(id),
            item_code VARCHAR(50) NOT NULL,
            customer_id BIGINT REFERENCES customers(id),
            customer_code VARCHAR(50),
            supplier_id BIGINT REFERENCES suppliers(id),
            supplier_code VARCHAR(50),
            base_price DECIMAL(12,2) NOT NULL,
            currency_code VARCHAR(3) NOT NULL DEFAULT 'JPY',
            valid_from_date DATE NOT NULL,
            valid_to_date DATE NOT NULL,
            status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
            CONSTRAINT uk_prices_item_customer_supplier_date_not_deleted 
                UNIQUE (price_type, item_id, customer_id, supplier_id, valid_from_date, valid_to_date, deleted)
        );
    END IF;
END $$;

-- 数量スケール価格テーブル
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'price_scales') THEN
        CREATE TABLE price_scales (
            id BIGSERIAL PRIMARY KEY,
            created_at TIMESTAMP NOT NULL,
            updated_at TIMESTAMP,
            created_by VARCHAR(100),
            updated_by VARCHAR(100),
            deleted BOOLEAN DEFAULT FALSE,
            price_id BIGINT NOT NULL REFERENCES prices(id),
            from_quantity DECIMAL(12,3) NOT NULL,
            to_quantity DECIMAL(12,3),
            scale_price DECIMAL(12,2) NOT NULL,
            CONSTRAINT uk_price_scales_quantity_not_deleted 
                UNIQUE (price_id, from_quantity, deleted)
        );
    END IF;
END $$;

-- mini_erp_userに必要な権限を付与
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO mini_erp_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO mini_erp_user;