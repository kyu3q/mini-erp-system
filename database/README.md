# Mini ERP System データベースセットアップ手順

## 前提条件
- MySQL 8.0以上がインストールされていること
- MySQLのrootユーザーまたは同等の権限を持つユーザーでログインできること

## セットアップ手順

1. データベースとユーザーの作成
```bash
mysql -u root -p < create_database.sql
```

2. テーブルの作成
```bash
mysql -u root -p mini_erp_db < create_tables.sql
```

## 接続情報
- データベース名: mini_erp_db
- ユーザー名: mini_erp_user
- パスワード: mini_erp_pass
- ホスト: localhost
- ポート: 3306（デフォルト）

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