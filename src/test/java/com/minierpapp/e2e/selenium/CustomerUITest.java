package com.minierpapp.e2e.selenium;

import com.minierpapp.e2e.page.CustomerPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CustomerUITest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private CustomerPage customerPage;
    private String baseUrl;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-software-rasterizer");
        options.addArguments("--disable-setuid-sandbox");
        options.addArguments("--disable-web-security");
        options.addArguments("--disable-features=IsolateOrigins,site-per-process");
        options.addArguments("--disable-dev-tools");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--ignore-ssl-errors");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-save-password-bubble");
        options.addArguments("--disable-translate");
        options.addArguments("--disable-logging");
        options.addArguments("--disable-permissions-api");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-blink-features");
        options.addArguments("--disable-blink-features=BlockCredentialedSubresources");
        options.addArguments("--disable-accelerated-2d-canvas");
        options.addArguments("--disable-accelerated-jpeg-decoding");
        options.addArguments("--disable-accelerated-mjpeg-decode");
        options.addArguments("--disable-accelerated-video-decode");
        options.addArguments("--disable-app-list-dismiss-on-blur");
        options.addArguments("--disable-background-timer-throttling");
        options.addArguments("--disable-backgrounding-occluded-windows");
        options.addArguments("--disable-breakpad");
        options.addArguments("--disable-component-extensions-with-background-pages");
        options.addArguments("--disable-default-apps");
        options.addArguments("--disable-demo-mode");
        options.addArguments("--disable-device-discovery-notifications");
        options.addArguments("--disable-domain-reliability");
        options.addArguments("--disable-extensions-http-throttling");
        options.addArguments("--disable-features=TranslateUI");
        options.addArguments("--disable-hang-monitor");
        options.addArguments("--disable-ipc-flooding-protection");
        options.addArguments("--disable-prompt-on-repost");
        options.addArguments("--disable-renderer-backgrounding");
        options.addArguments("--disable-sync");
        options.addArguments("--disable-threaded-scrolling");
        options.addArguments("--disable-webgl");
        options.addArguments("--disable-webgl2");
        options.addArguments("--enable-automation");
        options.addArguments("--enable-features=NetworkService,NetworkServiceInProcess");
        options.addArguments("--force-color-profile=srgb");
        options.addArguments("--hide-scrollbars");
        options.addArguments("--metrics-recording-only");
        options.addArguments("--mute-audio");
        options.addArguments("--no-default-browser-check");
        options.addArguments("--no-first-run");
        options.addArguments("--no-pings");
        options.addArguments("--no-zygote");
        options.addArguments("--password-store=basic");
        options.addArguments("--use-gl=swiftshader");
        options.addArguments("--use-mock-keychain");
        options.addArguments("--window-position=0,0");
        
        driver = new ChromeDriver(options);
        baseUrl = "http://localhost:" + port;
        driver.get(baseUrl);
        
        customerPage = new CustomerPage(driver);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void testCustomerCRUDOperations() {
        // 1. 得意先マスタ画面を開く
        customerPage.open();

        // 2. 新規登録
        customerPage.clickAddButton();
        customerPage.inputCustomerInfo(
            "UI001",
            "UIテスト株式会社",
            "ユーアイテストカブシキガイシャ",
            "123-4567",
            "東京都千代田区1-1-1",
            "03-1234-5678",
            "ui-test@example.com"
        );
        customerPage.clickSaveButton();

        // 保存成功を確認
        assertThat(customerPage.getSuccessMessage()).contains("保存しました");

        // 3. 検索
        customerPage.searchCustomer("UI001", "");
        assertThat(customerPage.isCustomerDisplayed("UI001")).isTrue();

        // 4. 詳細情報の確認
        CustomerPage.CustomerInfo info = customerPage.getCustomerInfo("UI001");
        assertThat(info.getName()).isEqualTo("UIテスト株式会社");
        assertThat(info.getNameKana()).isEqualTo("ユーアイテストカブシキガイシャ");
        assertThat(info.getPostalCode()).isEqualTo("123-4567");

        // 5. 編集
        customerPage.clickEditButton("UI001");
        customerPage.inputCustomerInfo(
            "UI001",
            "UIテスト株式会社（更新）",
            "ユーアイテストカブシキガイシャ",
            "123-4567",
            "東京都千代田区1-1-1",
            "03-1234-5678",
            "ui-test@example.com"
        );
        customerPage.clickSaveButton();

        // 更新成功を確認
        assertThat(customerPage.getSuccessMessage()).contains("保存しました");

        // 6. 更新内容の確認
        customerPage.searchCustomer("UI001", "");
        info = customerPage.getCustomerInfo("UI001");
        assertThat(info.getName()).isEqualTo("UIテスト株式会社（更新）");

        // 7. 削除
        customerPage.clickDeleteButton("UI001");
        assertThat(customerPage.getSuccessMessage()).contains("削除しました");

        // 8. 削除確認
        customerPage.searchCustomer("UI001", "");
        assertThat(customerPage.isCustomerDisplayed("UI001")).isFalse();
    }

    @Test
    void testCustomerValidation() {
        // 1. 得意先マスタ画面を開く
        customerPage.open();

        // 2. 必須項目が未入力の状態で保存
        customerPage.clickAddButton();
        customerPage.inputCustomerInfo("", "", "", "", "", "", "");
        customerPage.clickSaveButton();

        // エラーメッセージを確認
        assertThat(customerPage.getErrorMessage()).contains("得意先コードを入力してください");

        // 3. 重複する顧客コードで保存
        customerPage.inputCustomerInfo(
            "CUST001",  // 既存の顧客コード
            "重複テスト",
            "",
            "",
            "",
            "",
            ""
        );
        customerPage.clickSaveButton();

        // エラーメッセージを確認
        assertThat(customerPage.getErrorMessage()).contains("は既に使用されています");

        // 4. キャンセルボタンの動作確認
        customerPage.clickCancelButton();
        assertThat(customerPage.isCustomerDisplayed("CUST001")).isTrue();
    }

    @Test
    void testCustomerSearch() {
        // 1. 得意先マスタ画面を開く
        customerPage.open();

        // 2. 顧客コードで検索
        customerPage.searchCustomer("CUST001", "");
        List<String> results = customerPage.getCustomerCodes();
        assertThat(results).containsExactly("CUST001");

        // 3. 名前で検索
        customerPage.searchCustomer("", "テスト");
        results = customerPage.getCustomerCodes();
        assertThat(results).contains("CUST001", "CUST002", "CUST003");

        // 4. 複数条件で検索
        customerPage.searchCustomer("CUST", "1");
        results = customerPage.getCustomerCodes();
        assertThat(results).contains("CUST001");

        // 5. 該当なしの検索
        customerPage.searchCustomer("NONEXISTENT", "");
        results = customerPage.getCustomerCodes();
        assertThat(results).isEmpty();
    }

    @Test
    void testCustomerFormValidation() {
        // 1. 得意先マスタ画面を開く
        customerPage.open();

        // 2. 新規登録画面を開く
        customerPage.clickAddButton();

        // 3. 不正な郵便番号形式
        customerPage.inputCustomerInfo(
            "VALID001",
            "バリデーションテスト",
            "",
            "1234567",  // 不正な形式
            "",
            "",
            ""
        );
        customerPage.clickSaveButton();
        assertThat(customerPage.getErrorMessage()).contains("郵便番号の形式が正しくありません");

        // 4. 不正なメールアドレス形式
        customerPage.inputCustomerInfo(
            "VALID001",
            "バリデーションテスト",
            "",
            "123-4567",
            "",
            "",
            "invalid-email"  // 不正な形式
        );
        customerPage.clickSaveButton();
        assertThat(customerPage.getErrorMessage()).contains("メールアドレスの形式が正しくありません");

        // 5. 文字数制限超過
        String longText = "a".repeat(101);
        customerPage.inputCustomerInfo(
            "VALID001",
            longText,  // 100文字超過
            "",
            "123-4567",
            "",
            "",
            "test@example.com"
        );
        customerPage.clickSaveButton();
        assertThat(customerPage.getErrorMessage()).contains("得意先名は100文字以内で入力してください");
    }

    @Test
    void testExcelExport() throws IOException {
        // 1. 得意先マスタ画面を開く
        customerPage.open();

        // 2. Excel出力を実行
        String exportedFile = customerPage.exportExcel();

        // 3. 出力されたファイルの内容を検証
        Map<Integer, List<String>> content = customerPage.verifyExcelContent(exportedFile);
        assertThat(content).isNotEmpty();

        // 4. 出力された内容が画面の内容と一致することを確認
        List<String> customerCodes = customerPage.getCustomerCodes();
        assertThat(content.values().stream()
            .map(row -> row.get(0))  // 顧客コード列
            .toList())
            .containsAll(customerCodes);

        // 5. ファイルを削除
        new File(exportedFile).delete();
    }

    @Test
    void testExcelImport() throws IOException {
        // 1. 得意先マスタ画面を開く
        customerPage.open();

        // 2. テンプレートをダウンロード
        String templateFile = customerPage.downloadTemplate();
        assertThat(new File(templateFile)).exists();

        // 3. テンプレートの内容を検証
        Map<Integer, List<String>> template = customerPage.verifyExcelContent(templateFile);
        assertThat(template.get(0)).contains("顧客コード", "名前", "カナ名");

        // 4. テンプレートを使用してデータを取り込む
        String testDataFile = createTestDataFile();
        customerPage.importExcel(testDataFile);

        // 5. 取り込み結果を確認
        assertThat(customerPage.getSuccessMessage()).contains("取り込みが完了しました");
        assertThat(customerPage.isCustomerDisplayed("IMPORT001")).isTrue();

        // 6. 一時ファイルを削除
        new File(templateFile).delete();
        new File(testDataFile).delete();
    }

    @Test
    void testExcelImportValidation() throws IOException {
        // 1. 得意先マスタ画面を開く
        customerPage.open();

        // 2. 不正なデータを含むファイルを取り込む
        String invalidDataFile = createInvalidTestDataFile();
        customerPage.importExcel(invalidDataFile);

        // 3. エラーメッセージを確認
        assertThat(customerPage.getErrorMessage()).contains("データの形式が不正です");

        // 4. 重複データを含むファイルを取り込む
        String duplicateDataFile = createDuplicateTestDataFile();
        customerPage.importExcel(duplicateDataFile);

        // 5. エラーメッセージを確認
        assertThat(customerPage.getErrorMessage()).contains("既に存在する顧客コードが含まれています");

        // 6. 一時ファイルを削除
        new File(invalidDataFile).delete();
        new File(duplicateDataFile).delete();
    }

    /**
     * テスト用のExcelファイルを作成
     */
    private String createTestDataFile() throws IOException {
        String filePath = System.getProperty("java.io.tmpdir") + "/test_data.xlsx";
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("得意先");
            
            // ヘッダー行
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("顧客コード");
            headerRow.createCell(1).setCellValue("名前");
            headerRow.createCell(2).setCellValue("カナ名");
            
            // データ行
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("IMPORT001");
            dataRow.createCell(1).setCellValue("取込テスト株式会社");
            dataRow.createCell(2).setCellValue("トリコミテストカブシキガイシャ");
            
            workbook.write(new FileOutputStream(filePath));
        }
        return filePath;
    }

    /**
     * 不正なデータを含むテスト用Excelファイルを作成
     */
    private String createInvalidTestDataFile() throws IOException {
        String filePath = System.getProperty("java.io.tmpdir") + "/invalid_data.xlsx";
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("得意先");
            
            // ヘッダー行
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("顧客コード");
            headerRow.createCell(1).setCellValue("名前");
            
            // 不正なデータ行（必須項目が空）
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("");
            dataRow.createCell(1).setCellValue("不正データテスト");
            
            workbook.write(new FileOutputStream(filePath));
        }
        return filePath;
    }

    /**
     * 重複データを含むテスト用Excelファイルを作成
     */
    private String createDuplicateTestDataFile() throws IOException {
        String filePath = System.getProperty("java.io.tmpdir") + "/duplicate_data.xlsx";
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("得意先");
            
            // ヘッダー行
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("顧客コード");
            headerRow.createCell(1).setCellValue("名前");
            
            // 既存の顧客コードを使用
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("CUST001");
            dataRow.createCell(1).setCellValue("重複テスト");
            
            workbook.write(new FileOutputStream(filePath));
        }
        return filePath;
    }
}