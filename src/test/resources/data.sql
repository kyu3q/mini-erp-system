-- テストユーザーの追加
INSERT INTO users (username, password, enabled) VALUES
('test_user', '$2a$10$6HxXqHgI8Ulr4wiM5mVnUuWj0ZXAR7ieaPkJfI3o5TgX3VQ0sIhSi', true);

INSERT INTO authorities (username, authority) VALUES
('test_user', 'ROLE_USER');

-- テスト用得意先データの追加
INSERT INTO customers (customer_code, name, name_kana, postal_code, address, phone, email, fax, contact_person, payment_terms, status, notes, created_at, updated_at, created_by, updated_by, deleted) VALUES
('CUST001', 'テスト株式会社1', 'テストカブシキガイシャ1', '123-4567', '東京都千代田区1-1-1', '03-1234-5678', 'test1@example.com', '03-1234-5679', '山田太郎', '月末締め翌月末払い', 'ACTIVE', 'テスト用データ1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', false),
('CUST002', 'テスト株式会社2', 'テストカブシキガイシャ2', '234-5678', '東京都中央区2-2-2', '03-2345-6789', 'test2@example.com', '03-2345-6780', '鈴木一郎', '月末締め翌月末払い', 'ACTIVE', 'テスト用データ2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', false),
('CUST003', 'テスト株式会社3', 'テストカブシキガイシャ3', '345-6789', '東京都港区3-3-3', '03-3456-7890', 'test3@example.com', '03-3456-7891', '佐藤花子', '月末締め翌月末払い', 'INACTIVE', 'テスト用データ3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', false);