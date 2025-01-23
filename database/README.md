# Mini ERP System データベースセットアップ手順

## 環境別のデータベース設定

### 開発環境・テスト環境
開発環境とテスト環境では、H2インメモリデータベースが使用されます。
特別なセットアップは不要で、アプリケーション起動時に自動的にデータベースが作成されます。

- データベース: H2 (インメモリ)
- URL: jdbc:h2:mem:mini_erp_db (開発環境) / jdbc:h2:mem:testdb (テスト環境)
- ユーザー名: sa
- パスワード: なし

H2コンソールへのアクセス:
- URL: http://localhost:8080/h2-console
- JDBC URL: 上記のURLを使用

### 本番環境

#### 前提条件
- PostgreSQL 14以上がインストールされていること
- PostgreSQLのスーパーユーザー権限を持つユーザーでログインできること

#### セットアップ手順

1. データベースとユーザーの作成
```bash
psql -U postgres -f create_database.sql
```

2. テーブルの作成
```bash
psql -U postgres -d mini_erp_db -f create_tables.sql
```

#### 接続情報
- データベース名: mini_erp_db
- ユーザー名: mini_erp_user
- パスワード: mini_erp_pass
- ホスト: db (Docker環境) または localhost (ローカル環境)
- ポート: 5432（PostgreSQLデフォルト）

## テーブル構成

### 基本情報
- 商品（products）
- 倉庫（warehouses）
- 仕入先（suppliers）
- 顧客（customers）

### 在庫関連
- 在庫（stocks）: 日々の在庫数量を管理
- 棚卸（inventories）: 棚卸の記録を管理
- 棚卸明細（inventory_details）: 棚卸の詳細を管理

### 価格管理
- 販売価格（selling_prices）
- 購買価格（purchase_prices）

### 取引管理
- 受注（orders）
- 受注明細（order_details）
- 発注（purchase_orders）
- 発注明細（purchase_order_details）

### 入出庫管理
- 入庫（receiving）
- 入庫明細（receiving_details）
- 出荷（shipments）
- 出荷明細（shipment_details）