-- 既存のユニーク制約を削除
ALTER TABLE products DROP CONSTRAINT IF EXISTS products_product_code_key;

-- 論理削除を考慮した新しいユニーク制約を追加
ALTER TABLE products
    ADD CONSTRAINT uk_products_product_code_not_deleted UNIQUE (product_code, deleted);