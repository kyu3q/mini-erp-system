# 統合テスト仕様書

## 概要
mini-erp-systemの統合テストに関する仕様を記載します。

## テストの種類

### 1. データベース統合テスト（CustomerRepositoryIntegrationTest）

#### 1.1 目的
- JPA Repositoryとデータベースの連携が正しく動作することを確認
- データの永続化と取得が正しく行われることを確認

#### 1.2 テスト内容
| テストケース | 期待結果 | テストメソッド |
|------------|---------|--------------|
| 顧客コードによる検索 | 指定した顧客コードの得意先が取得できること | `whenFindByCustomerCodeAndDeletedFalse_thenReturnCustomer` |
| 複数条件での検索 | 条件に一致する得意先のリストが取得できること | `whenFindByCustomerCodeAndName_thenReturnFilteredCustomers` |
| ページング検索 | 指定したページサイズ分の得意先が取得できること | `whenFindByNameContainingAndDeletedFalse_thenReturnPagedResults` |
| 有効な得意先の検索 | 論理削除されていない得意先のみが取得できること | `whenFindByDeletedFalse_thenReturnActiveCustomers` |
| 得意先の保存 | 得意先情報が正しく保存されること | `whenSaveCustomer_thenCustomerIsPersisted` |
| 得意先の論理削除 | 得意先が論理削除され、検索できなくなること | `whenDeleteCustomer_thenCustomerIsMarkedAsDeleted` |
| 顧客コードの存在確認 | 顧客コードの重複チェックが正しく動作すること | `whenExistsByCustomerCodeAndDeletedFalse_thenReturnTrue` |
| キーワード検索 | コードまたは名前に一致する得意先が取得できること | `whenFindByCustomerCodeContainingOrNameContainingAndDeletedFalse_thenReturnMatchingCustomers` |

### 2. APIテスト（CustomerControllerIntegrationTest）

#### 2.1 目的
- RESTful APIのエンドポイントが正しく動作することを確認
- リクエスト・レスポンスの形式が正しいことを確認
- セキュリティ設定が正しく動作することを確認

#### 2.2 テスト内容
| テストケース | 期待結果 | テストメソッド |
|------------|---------|--------------|
| 得意先一覧の取得 | 得意先のリストが返却されること | `whenGetAllCustomers_thenReturnCustomersList` |
| 得意先の個別取得 | 指定した得意先の情報が返却されること | `whenGetCustomerByCode_thenReturnCustomer` |
| 得意先の新規作成 | 得意先が作成され、作成された情報が返却されること | `whenCreateCustomer_thenReturnCreatedCustomer` |
| 得意先の更新 | 得意先が更新され、更新後の情報が返却されること | `whenUpdateCustomer_thenReturnUpdatedCustomer` |
| 得意先の削除 | 得意先が削除され、204が返却されること | `whenDeleteCustomer_thenReturn204` |
| 得意先の検索 | 検索条件に一致する得意先が返却されること | `whenSearchCustomers_thenReturnFilteredCustomers` |
| 未認証アクセス | 401エラーが返却されること | `whenUnauthorizedAccess_thenReturn401` |
| 権限外アクセス | 403エラーが返却されること | `whenForbiddenAccess_thenReturn403` |
| 不正なデータ | 400エラーが返却されること | `whenCreateInvalidCustomer_thenReturn400` |
| 重複データ | 400エラーが返却されること | `whenCreateDuplicateCustomer_thenReturn400` |

### 3. エンドツーエンドテスト（CustomerE2ETest）

#### 3.1 目的
- システム全体を通した機能の動作確認
- ユースケースシナリオの検証
- データの整合性の確認

#### 3.2 テスト内容
| テストケース | 期待結果 | テストメソッド |
|------------|---------|--------------|
| 得意先のライフサイクル | 作成→参照→更新→削除の一連の流れが正しく動作すること | `testCustomerLifecycle` |
| 一括操作 | 複数の得意先の一括登録と検索が正しく動作すること | `testBulkOperations` |
| エラー処理 | 各種エラーケースが適切に処理されること | `testErrorHandling` |

## テスト環境

### 1. 使用技術
- Spring Boot Test
- JUnit 5
- AssertJ
- MockMvc
- H2 Database（テスト用インメモリDB）

### 2. 設定ファイル
- `application-test.properties`: テスト用の設定
- `data.sql`: テストデータの初期化

### 3. セキュリティ設定
- テストユーザー: test/test
- ロール: USER, ADMIN

## テストデータ

### 1. 初期データ
```sql
INSERT INTO customers (customer_code, name, ...) VALUES
('CUST001', 'テスト株式会社1', ...),
('CUST002', 'テスト株式会社2', ...),
('CUST003', 'テスト株式会社3', ...);
```

### 2. テストユーザー
```sql
INSERT INTO users (username, password, enabled) VALUES
('test_user', '$2a$10$...', true);

INSERT INTO authorities (username, authority) VALUES
('test_user', 'ROLE_USER');
```

## 注意事項

1. テストの独立性
   - 各テストは独立して実行可能
   - テストデータは各テスト実行時にリセット
   - トランザクション管理の適切な設定

2. 並行実行
   - テストの並行実行に対する考慮
   - データの競合を防ぐための設計

3. パフォーマンス
   - テストの実行時間の監視
   - 不要なデータベースアクセスの最小化

## 今後の課題

1. テストカバレッジの向上
   - エッジケースの追加
   - 異常系テストの拡充

2. パフォーマンステストの追加
   - 大量データでのテスト
   - 負荷テスト

3. セキュリティテストの拡充
   - より詳細な権限テスト
   - クロスサイトスクリプティング対策の検証

4. 結合テストの追加
   - 外部システムとの連携テスト
   - メッセージングシステムとの連携テスト