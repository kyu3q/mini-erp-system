-- 品目テーブル
CREATE TABLE IF NOT EXISTS items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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

-- 倉庫テーブル
CREATE TABLE IF NOT EXISTS warehouses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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

-- 在庫テーブル
CREATE TABLE IF NOT EXISTS stocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted BOOLEAN DEFAULT FALSE,
    warehouse_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    minimum_quantity INTEGER,
    maximum_quantity INTEGER,
    location VARCHAR(100),
    notes TEXT,
    CONSTRAINT uk_stocks_warehouse_item UNIQUE(warehouse_id, item_id, deleted),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    FOREIGN KEY (item_id) REFERENCES items(id)
);

-- 得意先テーブル
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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

-- 仕入先テーブル
CREATE TABLE IF NOT EXISTS suppliers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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

-- 受注テーブル
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted BOOLEAN DEFAULT FALSE,
    order_number VARCHAR(50) NOT NULL,
    order_date DATE NOT NULL,
    customer_id BIGINT NOT NULL,
    delivery_date DATE,
    shipping_address VARCHAR(200),
    shipping_postal_code VARCHAR(8),
    shipping_phone VARCHAR(20),
    shipping_contact_person VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    notes TEXT,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    CONSTRAINT uk_orders_order_number_not_deleted UNIQUE (order_number, deleted),
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- 受注明細テーブル
CREATE TABLE IF NOT EXISTS order_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted BOOLEAN DEFAULT FALSE,
    order_id BIGINT NOT NULL,
    line_number INTEGER NOT NULL,
    item_id BIGINT,
    item_name VARCHAR(100) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    warehouse_id BIGINT,
    delivery_date DATE,
    notes TEXT,
    CONSTRAINT uk_order_details_order_line UNIQUE(order_id, line_number, deleted),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
);

-- 単価マスタテーブル
CREATE TABLE IF NOT EXISTS prices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted BOOLEAN DEFAULT FALSE,
    price_type VARCHAR(20) NOT NULL,
    condition_type VARCHAR(30) NOT NULL,
    valid_from_date DATE NOT NULL,
    valid_to_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL
);

-- 単価マスタ明細（品目単位）テーブル
CREATE TABLE IF NOT EXISTS price_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    price_id BIGINT NOT NULL,
    item_code VARCHAR(50) NOT NULL,
    base_price DECIMAL(12,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    CONSTRAINT uk_price_items_price_item UNIQUE(price_id, item_code),
    FOREIGN KEY (price_id) REFERENCES prices(id)
);

-- 単価マスタ明細（仕入先・品目単位）テーブル
CREATE TABLE IF NOT EXISTS price_supplier_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    price_id BIGINT NOT NULL,
    supplier_code VARCHAR(50) NOT NULL,
    item_code VARCHAR(50) NOT NULL,
    base_price DECIMAL(12,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    CONSTRAINT uk_price_supplier_items_price_supplier_item UNIQUE(price_id, supplier_code, item_code),
    FOREIGN KEY (price_id) REFERENCES prices(id)
);

-- 単価マスタ明細（得意先・品目単位）テーブル
CREATE TABLE IF NOT EXISTS price_customer_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    price_id BIGINT NOT NULL,
    customer_code VARCHAR(50) NOT NULL,
    item_code VARCHAR(50) NOT NULL,
    base_price DECIMAL(12,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    CONSTRAINT uk_price_customer_items_price_customer_item UNIQUE(price_id, customer_code, item_code),
    FOREIGN KEY (price_id) REFERENCES prices(id)
);

-- 単価マスタ明細（仕入先・得意先・品目単位）テーブル
CREATE TABLE IF NOT EXISTS price_supplier_customer_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    price_id BIGINT NOT NULL,
    supplier_code VARCHAR(50) NOT NULL,
    customer_code VARCHAR(50) NOT NULL,
    item_code VARCHAR(50) NOT NULL,
    base_price DECIMAL(12,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    CONSTRAINT uk_price_supplier_customer_items_price_supplier_customer_item UNIQUE(price_id, supplier_code, customer_code, item_code),
    FOREIGN KEY (price_id) REFERENCES prices(id)
);

-- 数量スケール価格テーブル
CREATE TABLE IF NOT EXISTS price_scales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    price_id BIGINT NOT NULL,
    from_quantity DECIMAL(12,3) NOT NULL,
    to_quantity DECIMAL(12,3) NOT NULL,
    scale_price DECIMAL(12,2) NOT NULL,
    CONSTRAINT uk_price_scales_price_quantity UNIQUE(price_id, from_quantity, to_quantity),
    FOREIGN KEY (price_id) REFERENCES prices(id)
);
