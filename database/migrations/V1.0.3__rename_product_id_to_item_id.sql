-- Rename product_id to item_id in order_details table
ALTER TABLE order_details DROP CONSTRAINT fk_order_details_products;
ALTER TABLE order_details RENAME COLUMN product_id TO item_id;
ALTER TABLE order_details ADD CONSTRAINT fk_order_details_items FOREIGN KEY (item_id) REFERENCES items(id);