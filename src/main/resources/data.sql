-- 品目の初期データ
INSERT INTO items (
    created_at, created_by, item_code, item_name, description, unit, status, 
    minimum_stock, maximum_stock, reorder_point, deleted
) VALUES
    (CURRENT_TIMESTAMP, 'system', 'P001', 'ノートパソコン', '高性能ビジネスノートPC', '台', 'ACTIVE', 10, 100, 20, false),
    (CURRENT_TIMESTAMP, 'system', 'P002', 'デスクトップPC', 'オフィス用デスクトップPC', '台', 'ACTIVE', 5, 50, 10, false),
    (CURRENT_TIMESTAMP, 'system', 'P003', 'USBメモリ 32GB', '高速USBメモリ', '個', 'ACTIVE', 50, 500, 100, false),
    (CURRENT_TIMESTAMP, 'system', 'P004', 'ワイヤレスマウス', 'エルゴノミクスデザイン', '個', 'ACTIVE', 20, 200, 50, false),
    (CURRENT_TIMESTAMP, 'system', 'P005', 'モニター27インチ', '4K対応ディスプレイ', '台', 'ACTIVE', 10, 80, 20, false);

-- 倉庫の初期データ
INSERT INTO warehouses (
    created_at, created_by, warehouse_code, name, address, capacity, status, description, deleted
) VALUES
    (CURRENT_TIMESTAMP, 'system', 'W001', '東京メイン倉庫', '東京都江東区豊洲1-1-1', 1000, 'ACTIVE', '主要保管施設', false),
    (CURRENT_TIMESTAMP, 'system', 'W002', '大阪支店倉庫', '大阪府大阪市北区梅田2-2-2', 500, 'ACTIVE', '関西地域の配送拠点', false),
    (CURRENT_TIMESTAMP, 'system', 'W003', '福岡物流センター', '福岡県福岡市博多区博多駅3-3-3', 300, 'ACTIVE', '九州地域の物流拠点', false);

-- 在庫の初期データ
INSERT INTO stocks (
    created_at, created_by, warehouse_id, item_id, quantity, minimum_quantity, maximum_quantity, location, notes, deleted
) VALUES
    (CURRENT_TIMESTAMP, 'system', 1, 1, 50, 10, 100, 'A-1-1', 'ノートPC保管エリア', false),
    (CURRENT_TIMESTAMP, 'system', 1, 2, 30, 5, 50, 'A-1-2', 'デスクトップPC保管エリア', false),
    (CURRENT_TIMESTAMP, 'system', 1, 3, 200, 50, 500, 'B-1-1', 'USBメモリ保管棚', false),
    (CURRENT_TIMESTAMP, 'system', 2, 1, 25, 5, 50, 'A-1-1', '大阪ノートPC在庫', false),
    (CURRENT_TIMESTAMP, 'system', 2, 4, 100, 20, 200, 'B-2-1', 'マウス保管エリア', false),
    (CURRENT_TIMESTAMP, 'system', 3, 5, 40, 10, 80, 'C-1-1', 'モニター保管エリア', false);

-- 得意先の初期データ
INSERT INTO customers (
    created_at, created_by, customer_code, name, name_kana, postal_code, address,
    phone, email, status, deleted
) VALUES
    (CURRENT_TIMESTAMP, 'system', 'C001', '株式会社ABC商事', 'カブシキガイシャエービーシーショウジ', '100-0001', '東京都千代田区丸の内1-1-1',
     '03-1234-5678', 'contact@abc-trading.example.com', 'ACTIVE', false),
    (CURRENT_TIMESTAMP, 'system', 'C002', '株式会社XYZ物産', 'カブシキガイシャエックスワイゼットブッサン', '541-0041', '大阪府大阪市中央区北浜2-2-2',
     '06-2345-6789', 'info@xyz-trading.example.com', 'ACTIVE', false);

-- 仕入先の初期データ
INSERT INTO suppliers (
    created_at, created_by, supplier_code, name, name_kana, postal_code, address,
    phone, email, status, deleted
) VALUES
    (CURRENT_TIMESTAMP, 'system', 'S001', '株式会社テクノサプライ', 'カブシキガイシャテクノサプライ', '108-0075', '東京都港区港南3-3-3',
     '03-3456-7890', 'contact@techno-supply.example.com', 'ACTIVE', false),
    (CURRENT_TIMESTAMP, 'system', 'S002', '株式会社グローバルパーツ', 'カブシキガイシャグローバルパーツ', '532-0003', '大阪府大阪市淀川区宮原4-4-4',
     '06-4567-8901', 'info@global-parts.example.com', 'ACTIVE', false);
