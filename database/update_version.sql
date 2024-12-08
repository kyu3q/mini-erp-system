-- 既存のデータのバージョンを0に更新
UPDATE products SET version = 0 WHERE version IS NULL;
UPDATE warehouses SET version = 0 WHERE version IS NULL;
UPDATE stocks SET version = 0 WHERE version IS NULL;

-- バージョンカラムをNOT NULL制約に変更
ALTER TABLE products ALTER COLUMN version SET NOT NULL;
ALTER TABLE warehouses ALTER COLUMN version SET NOT NULL;
ALTER TABLE stocks ALTER COLUMN version SET NOT NULL;

-- バージョンカラムにデフォルト値を設定
ALTER TABLE products ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE warehouses ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE stocks ALTER COLUMN version SET DEFAULT 0;