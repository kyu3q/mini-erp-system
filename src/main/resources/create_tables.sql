-- 価格条件マスタ
CREATE TABLE IF NOT EXISTS price_conditions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted BOOLEAN DEFAULT FALSE,
    price_type VARCHAR(20) NOT NULL CHECK (price_type IN ('SALES', 'PURCHASE')),
    item_id BIGINT NOT NULL,
    item_code VARCHAR(50) NOT NULL,
    customer_id BIGINT,
    customer_code VARCHAR(50),
    supplier_id BIGINT,
    supplier_code VARCHAR(50),
    base_price DECIMAL(12,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'JPY',
    valid_from_date DATE NOT NULL,
    valid_to_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uk_price_conditions_item_customer_supplier_date_not_deleted 
        UNIQUE (price_type, item_id, customer_id, supplier_id, valid_from_date, valid_to_date, deleted),
    FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

-- 数量スケール価格テーブル
CREATE TABLE IF NOT EXISTS price_scales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted BOOLEAN DEFAULT FALSE,
    price_condition_id BIGINT NOT NULL,
    from_quantity DECIMAL(12,3) NOT NULL,
    to_quantity DECIMAL(12,3),
    scale_price DECIMAL(12,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'JPY',
    CONSTRAINT uk_price_scales_condition_quantity_not_deleted 
        UNIQUE (price_condition_id, from_quantity, deleted),
    FOREIGN KEY (price_condition_id) REFERENCES price_conditions(id)
);