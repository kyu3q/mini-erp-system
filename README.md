# Mini ERP System

販売管理、購買管理、在庫管理を統合したミニERPシステム

## 機能概要

### 1. 販売管理
- 受注管理
- 出荷管理
- 販売価格管理
- 顧客管理

### 2. 購買管理
- 発注管理
- 入庫管理
- 購買価格管理
- 仕入先管理

### 3. 在庫管理
- 在庫数量管理
- 棚卸管理
- 倉庫管理
- 商品管理

## 技術スタック

- Java 17
- Spring Boot 3.2.1
- Spring Security
- Spring Data JPA
- H2 Database (開発・テスト環境)
- PostgreSQL (本番環境)
- Maven
- Lombok

## セットアップ手順

### 1. 前提条件
- JDK 17以上
- Maven 3.6以上
- PostgreSQL 14以上（本番環境用）

### 2. データベースのセットアップ
データベースのセットアップ手順は [database/README.md](database/README.md) を参照してください。

### 3. アプリケーションの起動
```bash
mvn spring-boot:run
```

## 開発環境

### ビルド方法
```bash
mvn clean install
```

### テストの実行
```bash
mvn test
```

## API仕様
※ 今後、Swagger UIを使用してAPI仕様書を提供予定