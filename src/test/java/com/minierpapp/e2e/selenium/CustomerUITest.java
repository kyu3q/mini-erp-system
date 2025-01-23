package com.minierpapp.e2e.selenium;

import com.minierpapp.e2e.page.CustomerPage;
import io.github.bonigarcia.wdm.WebDriverManager;
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

import java.util.List;

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
        options.addArguments("--headless");  // ヘッドレスモードで実行
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        
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
}