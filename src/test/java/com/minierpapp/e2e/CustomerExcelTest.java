package com.minierpapp.e2e;

import com.minierpapp.e2e.page.CustomerPage;
import com.minierpapp.service.CustomerService;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CustomerExcelTest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    @Autowired
    private CustomerService customerService;
    private CustomerPage customerPage;

    @BeforeEach
    void setUp() {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        options.addArguments("--width=1920");
        options.addArguments("--height=1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addPreference("network.http.phishy-userpass-length", 255);
        options.addPreference("browser.download.folderList", 2);
        options.addPreference("browser.download.dir", System.getProperty("java.io.tmpdir"));
        options.addPreference("browser.helperApps.neverAsk.saveToDisk", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        options.addPreference("browser.download.manager.showWhenStarting", false);
        options.addPreference("browser.download.manager.focusWhenStarting", false);
        options.addPreference("browser.download.useDownloadDir", true);
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
        driver.manage().window().setSize(new Dimension(1920, 1080));
        customerPage = new CustomerPage(driver);

        // データベースのクリーンアップ
        customerService.deleteAll();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @WithMockUser(username = "test", roles = "ADMIN")
    void testExcelExportAndImport() throws IOException {
        // 1. 得意先を登録
        customerPage.open("http://localhost:" + port);
        customerPage.clickAddButton();
        customerPage.inputCustomerInfo(
            "EXL001",
            "Excel株式会社",
            "エクセルカブシキガイシャ",
            "100-0001",
            "東京都千代田区1-1-1",
            "03-1234-5678",
            "excel@example.com"
        );
        customerPage.clickSaveButton();
        assertThat(customerPage.getSuccessMessage()).contains("保存しました");

        // 2. Excelファイルをエクスポート
        String exportedFile = customerPage.exportExcel();
        assertThat(new File(exportedFile)).exists();

        // 3. エクスポートされたファイルの内容を検証
        Map<Integer, List<String>> content = customerPage.verifyExcelContent(exportedFile);
        assertThat(content).isNotEmpty();
        List<String> firstRow = content.get(1);  // 最初のデータ行
        assertThat(firstRow).containsExactly(
            "EXL001",
            "Excel株式会社",
            "エクセルカブシキガイシャ",
            "100-0001",
            "東京都千代田区1-1-1",
            "03-1234-5678",
            "excel@example.com",
            "",  // 担当者
            ""   // 備考
        );

        // 4. テンプレートをダウンロード
        String templateFile = customerPage.downloadTemplate();
        assertThat(new File(templateFile)).exists();

        // 5. 登録した得意先を削除
        customerPage.clickDeleteButton("EXL001");
        assertThat(customerPage.getSuccessMessage()).contains("削除しました");

        // 6. エクスポートしたファイルを使って取り込み
        customerPage.importExcel(exportedFile);
        assertThat(customerPage.getSuccessMessage()).contains("取り込みが完了しました");

        // 7. 取り込んだデータを確認
        assertThat(customerPage.isCustomerDisplayed("EXL001")).isTrue();
        CustomerPage.CustomerInfo importedInfo = customerPage.getCustomerInfo("EXL001");
        assertThat(importedInfo.getName()).isEqualTo("Excel株式会社");
        assertThat(importedInfo.getEmail()).isEqualTo("excel@example.com");

        // クリーンアップ
        customerPage.clickDeleteButton("EXL001");
    }

    @Test
    @WithMockUser(username = "test", roles = "ADMIN")
    void testExcelBulkImport() throws IOException {
        // 1. テンプレートをダウンロード
        customerPage.open("http://localhost:" + port);
        String templateFile = customerPage.downloadTemplate();
        assertThat(new File(templateFile)).exists();

        // 2. 複数の得意先を一括登録
        customerPage.importExcel(templateFile);
        assertThat(customerPage.getSuccessMessage()).contains("取り込みが完了しました");

        // 3. 一括登録されたデータを確認
        customerPage.searchCustomer("BULK", "");
        List<String> customerCodes = customerPage.getCustomerCodes();
        assertThat(customerCodes).hasSize(3);
        assertThat(customerCodes).contains("BULK001", "BULK002", "BULK003");

        // クリーンアップ
        for (String code : customerCodes) {
            customerPage.clickDeleteButton(code);
        }
    }
}