# 価格管理機能 設計書

## 1. 概要

### 1.1 目的
本機能は、商品の販売価格および購買価格を柔軟に管理するためのものです。以下の要件を満たすように設計されています：
- 品目ごとの基本価格の管理
- 取引先（得意先/仕入先）ごとの個別価格の管理
- 数量に応じた段階的な価格設定
- 有効期間による価格の管理
- 複数通貨への対応

### 1.2 機能範囲
- 価格条件の登録・編集・削除
- 数量スケール価格の設定
- 価格の一括インポート/エクスポート
- 価格履歴の管理
- 有効期限切れ価格の管理

## 2. システム構成

### 2.1 データベース構造

#### 価格条件マスタ (price_conditions)
| カラム名 | 型 | 説明 | 備考 |
|---------|-----|------|------|
| id | BIGINT | 主キー | 自動採番 |
| created_at | TIMESTAMP | 作成日時 | NOT NULL |
| updated_at | TIMESTAMP | 更新日時 | |
| created_by | VARCHAR(100) | 作成者 | |
| updated_by | VARCHAR(100) | 更新者 | |
| deleted | BOOLEAN | 論理削除フラグ | DEFAULT FALSE |
| price_type | VARCHAR(20) | 価格タイプ | 'SALES'/'PURCHASE' |
| item_id | BIGINT | 品目ID | NOT NULL, FK |
| item_code | VARCHAR(20) | 品目コード | NOT NULL |
| customer_id | BIGINT | 得意先ID | FK, NULL許容 |
| customer_code | VARCHAR(20) | 得意先コード | NULL許容 |
| supplier_id | BIGINT | 仕入先ID | FK, NULL許容 |
| supplier_code | VARCHAR(20) | 仕入先コード | NULL許容 |
| base_price | DECIMAL(12,2) | 基本価格 | NOT NULL |
| currency_code | VARCHAR(3) | 通貨コード | DEFAULT 'JPY' |
| valid_from_date | DATE | 有効開始日 | NOT NULL |
| valid_to_date | DATE | 有効終了日 | NOT NULL |
| status | VARCHAR(20) | ステータス | DEFAULT 'ACTIVE' |

#### 数量スケール価格 (price_scales)
| カラム名 | 型 | 説明 | 備考 |
|---------|-----|------|------|
| id | BIGINT | 主キー | 自動採番 |
| created_at | TIMESTAMP | 作成日時 | NOT NULL |
| updated_at | TIMESTAMP | 更新日時 | |
| created_by | VARCHAR(100) | 作成者 | |
| updated_by | VARCHAR(100) | 更新者 | |
| deleted | BOOLEAN | 論理削除フラグ | DEFAULT FALSE |
| price_condition_id | BIGINT | 価格条件ID | NOT NULL, FK |
| from_quantity | DECIMAL(12,3) | 数量範囲（開始） | NOT NULL |
| to_quantity | DECIMAL(12,3) | 数量範囲（終了） | NULL許容 |
| scale_price | DECIMAL(12,2) | スケール価格 | NOT NULL |

### 2.2 価格条件の優先順位
1. 取引先個別価格（得意先または仕入先指定）
2. 品目基本価格

### 2.3 数量スケールの適用ルール
1. 指定された数量が範囲内の価格を適用
2. 該当する数量範囲がない場合は基本価格を適用
3. to_quantityがNULLの場合、その範囲以上の全ての数量に適用

## 3. 画面設計

### 3.1 販売単価一覧画面
- 検索条件
  - 品目コード/名称
  - 得意先コード/名称
  - 有効期間
  - ステータス
- 一覧表示項目
  - 品目情報（コード、名称）
  - 得意先情報（コード、名称）※個別価格の場合のみ
  - 基本価格
  - 通貨
  - 有効期間
  - ステータス
- 機能ボタン
  - 新規登録
  - 編集
  - 削除（論理削除）
  - Excel出力
  - Excel取込
  - 取込テンプレート

### 3.2 購買単価一覧画面
- 検索条件
  - 品目コード/名称
  - 仕入先コード/名称
  - 得意先コード/名称
  - 有効期間
  - ステータス
- 一覧表示項目
  - 品目情報（コード、名称）
  - 仕入先情報（コード、名称）
  - 得意先情報（コード、名称）※個別価格の場合のみ
  - 基本価格
  - 通貨
  - 有効期間
  - ステータス
- 機能ボタン
  - 新規登録
  - 編集
  - 削除（論理削除）
  - Excel出力
  - Excel取込
  - 取込テンプレート

### 3.3 販売単価登録/編集画面
- 基本情報セクション
  - 品目選択（必須）
    - コード入力による直接指定
    - コード/名称での検索機能
    - 選択後は変更不可
    - コード入力時に名称を自動補完
  - 得意先選択（任意）
    - コード入力による直接指定
    - コード/名称での検索機能
    - 選択後は変更不可
    - コード入力時に名称を自動補完
  - 通貨選択（必須）
    - 選択後は変更不可
- 価格情報セクション
  - 有効開始日（必須）
  - 有効終了日（必須）
  - 基本価格（必須）
  - ステータス（必須）
- 数量スケール価格セクション
  - スケール価格テーブル
    - ドラッグ&ドロップによる並べ替え可能
    - 開始数量（必須）
    - 終了数量（任意）
    - スケール価格（必須）
  - スケール追加ボタン
  - スケール削除ボタン
- 機能ボタン
  - 保存
  - キャンセル

### 3.4 購買単価登録/編集画面
- 基本情報セクション
  - 品目選択（必須）
    - コード入力による直接指定
    - コード/名称での検索機能
    - 選択後は変更不可
    - コード入力時に名称を自動補完
  - 仕入先選択（必須）
    - コード入力による直接指定
    - コード/名称での検索機能
    - 選択後は変更不可
    - コード入力時に名称を自動補完
  - 得意先選択（任意）
    - コード入力による直接指定
    - コード/名称での検索機能
    - 選択後は変更不可
    - コード入力時に名称を自動補完
  - 通貨選択（必須）
    - 選択後は変更不可
- 価格情報セクション
  [販売単価と同様]
- 数量スケール価格セクション
  [販売単価と同様]
- 機能ボタン
  - 保存
  - キャンセル

### 3.5 数量スケール価格テーブル
```plaintext
┌─ 数量スケール価格 ──────────────────────┐
│ ┌──────────────────────────┐ │
│ │ [スケール追加]                                │ │
│ │                                              │ │
│ │ ┌────────────────────────┐│ │
│ │ │No.│開始数量│終了数量│スケール価格│削除│    ││ │
│ │ │1  │[数値]  │[数値]  │[金額]    │[×]│    ││ │
│ │ │2  │[数値]  │[数値]  │[金額]    │[×]│    ││ │
│ │ │3  │[数値]  │[数値]  │[金額]    │[×]│    ││ │
│ │ └────────────────────────┘│ │
│ │                                              │ │
│ │ ※ドラッグ&ドロップで並び替え可能             │ │
│ └──────────────────────────┘ │
└────────────────────────────────┘
```

#### スケール価格テーブルの特徴
1. インライン編集
   - メインフォーム内に直接表示
   - モーダル表示を使用しない
   - 即時編集可能

2. 操作性
   - ドラッグ&ドロップによる並び替え
   - 行の追加/削除が容易
   - 数値入力時の自動計算

3. バリデーション
   - 開始数量の必須チェック
   - スケール価格の必須チェック
   - 数量範囲の重複チェック
   - 数値の妥当性チェック

### 3.6 Excel関連機能

#### 3.6.1 Excel出力機能
- 検索条件に該当するデータをExcel形式で出力
- 出力項目は取込テンプレートと同一フォーマット
- ファイル名：
  - 販売単価：sales_price_YYYYMMDD.xlsx
  - 購買単価：purchase_price_YYYYMMDD.xlsx

#### 3.6.2 Excel取込機能
- 取込実行時の処理フロー
  1. ファイル形式チェック
  2. ヘッダー項目チェック
  3. データ形式チェック
  4. マスタ整合性チェック
  5. 一括登録処理
  6. 結果のポップアップ表示
     ```plaintext
     ┌─ 取込結果 ─────────────┐
     │ 処理件数：XX件                  │
     │ 成功件数：XX件                  │
     │ 失敗件数：XX件                  │
     │                                │
     │ ▼エラー内容                    │
     │ X行目：[エラーメッセージ]        │
     │ X行目：[エラーメッセージ]        │
     │                                │
     │ [OK]                          │
     └────────────────────┘
     ```

#### 3.6.3 取込テンプレート
- 販売単価テンプレート（sales_price_template.xlsx）
  - シート名：販売単価
  - 入力規則の設定
    - 通貨コード：リスト選択
    - 日付：YYYY/MM/DD形式
    - 状態：ACTIVE/INACTIVE

- 購買単価テンプレート（purchase_price_template.xlsx）
  - シート名：購買単価
  - 入力規則の設定
    - 通貨コード：リスト選択
    - 日付：YYYY/MM/DD形式
    - 状態：ACTIVE/INACTIVE

## 4. 機能詳細

### 4.1 価格の検索・照会
1. 価格条件の検索
   - 複数の検索条件を組み合わせて検索可能
   - 有効期間による絞り込み
   - 取引先指定の有無による絞り込み

2. 価格の照会
   - 指定された条件（品目、取引先、数量、日付）に該当する価格を返却
   - 優先順位に従って適用される価格を決定

### 4.2 価格の登録・更新
1. 入力チェック
   - 必須項目の確認
   - 有効期間の妥当性チェック
   - 数量範囲の重複チェック
   - 通貨コードの有効性チェック

2. 重複チェック
   - 同一条件（品目、取引先、期間）の価格設定の重複防止
   - 期間の重複がある場合はエラー

3. 数量スケール価格の管理
   - 数量範囲の連続性チェック
   - 範囲の重複チェック
   - 基本価格との整合性確認

### 4.3 Excel処理

#### 4.3.1 Excel出力処理
```java
@Service
public class PriceExcelService {
    public void exportSalesPrices(List<PriceCondition> conditions, OutputStream out) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("販売単価");
        
        // ヘッダー行の作成
        createSalesHeader(sheet);
        
        // データ行の作成
        int rowNum = 1;
        for (PriceCondition condition : conditions) {
            Row row = sheet.createRow(rowNum++);
            setBasicInfo(row, condition);
            setScalePrices(row, condition.getPriceScales());
        }
        
        workbook.write(out);
    }
    
    public void exportPurchasePrices(List<PriceCondition> conditions, OutputStream out) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("購買単価");
        
        // ヘッダー行の作成
        createPurchaseHeader(sheet);
        
        // データ行の作成
        int rowNum = 1;
        for (PriceCondition condition : conditions) {
            Row row = sheet.createRow(rowNum++);
            setBasicInfo(row, condition);
            setScalePrices(row, condition.getPriceScales());
        }
        
        workbook.write(out);
    }
}
```

#### 4.3.2 Excel取込処理
```java
@Service
public class PriceExcelService {
    @Transactional
    public ImportResult importSalesPrices(MultipartFile file) {
        List<SalesPriceExcelRow> rows = readExcel(file);
        ImportResult result = new ImportResult();
        
        for (SalesPriceExcelRow row : rows) {
            try {
                validateSalesPrice(row);
                PriceCondition condition = createOrUpdateSalesPrice(row);
                result.addSuccess(row.getRowNum());
            } catch (ValidationException e) {
                result.addError(row.getRowNum(), e.getMessage());
            }
        }
        
        return result;
    }
    
    @Transactional
    public ImportResult importPurchasePrices(MultipartFile file) {
        List<PurchasePriceExcelRow> rows = readExcel(file);
        ImportResult result = new ImportResult();
        
        for (PurchasePriceExcelRow row : rows) {
            try {
                validatePurchasePrice(row);
                PriceCondition condition = createOrUpdatePurchasePrice(row);
                result.addSuccess(row.getRowNum());
            } catch (ValidationException e) {
                result.addError(row.getRowNum(), e.getMessage());
            }
        }
        
        return result;
    }
}
```

#### 4.3.3 バリデーションルール

1. 共通バリデーション
   - 必須項目チェック
   - 日付形式チェック
   - 数値形式チェック
   - スケール数量の昇順チェック
   - スケール価格のペアチェック
   - 有効期間の妥当性チェック

2. 販売単価固有のバリデーション
   - 仕入先コードが指定されていないこと
   - 得意先コードが指定される場合、マスタに存在すること
   - 得意先の重複期間チェック

3. 購買単価固有のバリデーション
   - 仕入先コードが必須
   - 仕入先コードがマスタに存在すること
   - 得意先コードが指定される場合、マスタに存在すること
   - 仕入先・得意先の組み合わせの重複期間チェック

#### 4.3.4 エラーメッセージ
| エラーコード | メッセージ | 説明 |
|------------|------------|------|
| E001 | 必須項目が未入力です：{0} | |
| E002 | 日付の形式が不正です：{0} | |
| E003 | 数値の形式が不正です：{0} | |
| E004 | スケール数量が昇順になっていません | |
| E005 | スケール価格がペアで設定されていません | |
| E006 | 有効期間が不正です | |
| E007 | 販売単価に仕入先は指定できません | |
| E008 | 仕入先コードは必須です | |
| E009 | 得意先コードが存在しません：{0} | |
| E010 | 仕入先コードが存在しません：{0} | |
| E011 | 期間が重複しています | |

### 4.4 履歴管理
1. 変更履歴の保存
   - 変更日時
   - 変更者
   - 変更内容

2. 履歴の照会
   - 価格条件ごとの変更履歴表示
   - 変更前後の値の比較

## 5. 非機能要件

### 5.1 性能要件
- 一覧画面の表示：1秒以内
- 価格検索：0.5秒以内
- 一括処理：1000件/分以上

### 5.2 セキュリティ要件
- 価格情報へのアクセス制限
- 操作ログの記録
- 変更履歴の保持

### 5.3 運用要件
- バッチ処理による有効期限切れ通知
- 定期的なデータバックアップ
- アクセスログの保持

## 6. 今後の拡張性

### 6.1 将来的な機能拡張
1. 価格計算ロジックの拡張
   - 複数の計算方式（掛率、マージン等）
   - 通貨換算レートの自動適用

2. 承認ワークフロー
   - 価格変更の承認プロセス
   - 承認権限の管理

3. 分析機能
   - 価格トレンド分析
   - 収益性分析
   - 価格シミュレーション

### 6.2 システム連携
1. 外部システムとの連携
   - ERPシステム
   - 会計システム
   - 在庫管理システム

2. API提供
   - 価格照会API
   - 価格更新API
   - 一括処理API