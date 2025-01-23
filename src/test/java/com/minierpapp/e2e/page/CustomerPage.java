package com.minierpapp.e2e.page;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 得意先マスタ画面のページオブジェクト
 */
public class CustomerPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ページ要素の定義
    @FindBy(id = "customerListTable")
    private WebElement customerTable;

    @FindBy(id = "addCustomerButton")
    private WebElement addButton;

    @FindBy(id = "searchCustomerCode")
    private WebElement searchCustomerCodeInput;

    @FindBy(id = "searchCustomerName")
    private WebElement searchCustomerNameInput;

    @FindBy(id = "searchButton")
    private WebElement searchButton;

    @FindBy(id = "customerCode")
    private WebElement customerCodeInput;

    @FindBy(id = "name")
    private WebElement nameInput;

    @FindBy(id = "nameKana")
    private WebElement nameKanaInput;

    @FindBy(id = "postalCode")
    private WebElement postalCodeInput;

    @FindBy(id = "address")
    private WebElement addressInput;

    @FindBy(id = "phone")
    private WebElement phoneInput;

    @FindBy(id = "email")
    private WebElement emailInput;

    @FindBy(id = "saveButton")
    private WebElement saveButton;

    @FindBy(id = "cancelButton")
    private WebElement cancelButton;

    @FindBy(className = "alert-success")
    private WebElement successMessage;

    @FindBy(className = "alert-danger")
    private WebElement errorMessage;

    @FindBy(id = "exportExcelButton")
    private WebElement exportExcelButton;

    @FindBy(id = "importExcelButton")
    private WebElement importExcelButton;

    @FindBy(id = "downloadTemplateButton")
    private WebElement downloadTemplateButton;

    @FindBy(id = "fileInput")
    private WebElement fileInput;

    public CustomerPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    /**
     * 得意先マスタ画面を開く
     */
    public void open() {
        driver.get("/customers");
        wait.until(ExpectedConditions.visibilityOf(customerTable));
    }

    /**
     * 得意先を検索する
     */
    public void searchCustomer(String code, String name) {
        searchCustomerCodeInput.clear();
        searchCustomerCodeInput.sendKeys(code);
        searchCustomerNameInput.clear();
        searchCustomerNameInput.sendKeys(name);
        searchButton.click();
        wait.until(ExpectedConditions.visibilityOf(customerTable));
    }

    /**
     * 新規登録ボタンをクリック
     */
    public void clickAddButton() {
        addButton.click();
        wait.until(ExpectedConditions.visibilityOf(customerCodeInput));
    }

    /**
     * 得意先情報を入力
     */
    public void inputCustomerInfo(String code, String name, String nameKana, String postalCode,
                                String address, String phone, String email) {
        customerCodeInput.clear();
        customerCodeInput.sendKeys(code);
        nameInput.clear();
        nameInput.sendKeys(name);
        nameKanaInput.clear();
        nameKanaInput.sendKeys(nameKana);
        postalCodeInput.clear();
        postalCodeInput.sendKeys(postalCode);
        addressInput.clear();
        addressInput.sendKeys(address);
        phoneInput.clear();
        phoneInput.sendKeys(phone);
        emailInput.clear();
        emailInput.sendKeys(email);
    }

    /**
     * 保存ボタンをクリック
     */
    public void clickSaveButton() {
        saveButton.click();
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOf(successMessage),
            ExpectedConditions.visibilityOf(errorMessage)
        ));
    }

    /**
     * キャンセルボタンをクリック
     */
    public void clickCancelButton() {
        cancelButton.click();
        wait.until(ExpectedConditions.visibilityOf(customerTable));
    }

    /**
     * 得意先の編集ボタンをクリック
     */
    public void clickEditButton(String customerCode) {
        WebElement editButton = driver.findElement(
            By.xpath("//tr[contains(.,'" + customerCode + "')]//button[contains(@class,'edit-button')]")
        );
        editButton.click();
        wait.until(ExpectedConditions.visibilityOf(customerCodeInput));
    }

    /**
     * 得意先の削除ボタンをクリック
     */
    public void clickDeleteButton(String customerCode) {
        WebElement deleteButton = driver.findElement(
            By.xpath("//tr[contains(.,'" + customerCode + "')]//button[contains(@class,'delete-button')]")
        );
        deleteButton.click();
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
        wait.until(ExpectedConditions.visibilityOf(successMessage));
    }

    /**
     * 検索結果の得意先コードのリストを取得
     */
    public List<String> getCustomerCodes() {
        return driver.findElements(By.cssSelector("#customerListTable tbody tr td:first-child"))
            .stream()
            .map(WebElement::getText)
            .toList();
    }

    /**
     * 成功メッセージを取得
     */
    public String getSuccessMessage() {
        return wait.until(ExpectedConditions.visibilityOf(successMessage)).getText();
    }

    /**
     * エラーメッセージを取得
     */
    public String getErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOf(errorMessage)).getText();
    }

    /**
     * 得意先が表示されているか確認
     */
    public boolean isCustomerDisplayed(String customerCode) {
        return !driver.findElements(By.xpath("//tr[contains(.,'" + customerCode + "')]")).isEmpty();
    }

    /**
     * 得意先の詳細情報を取得
     */
    public CustomerInfo getCustomerInfo(String customerCode) {
        WebElement row = driver.findElement(By.xpath("//tr[contains(.,'" + customerCode + "')]"));
        List<WebElement> cells = row.findElements(By.tagName("td"));
        
        CustomerInfo info = new CustomerInfo();
        info.setCustomerCode(cells.get(0).getText());
        info.setName(cells.get(1).getText());
        info.setNameKana(cells.get(2).getText());
        info.setPostalCode(cells.get(3).getText());
        info.setAddress(cells.get(4).getText());
        info.setPhone(cells.get(5).getText());
        info.setEmail(cells.get(6).getText());
        return info;
    }

    /**
     * 得意先情報を保持するための内部クラス
     */
    public static class CustomerInfo {
        private String customerCode;
        private String name;
        private String nameKana;
        private String postalCode;
        private String address;
        private String phone;
        private String email;

        // Getters and Setters
        public String getCustomerCode() { return customerCode; }
        public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getNameKana() { return nameKana; }
        public void setNameKana(String nameKana) { this.nameKana = nameKana; }
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    /**
     * Excel出力を実行
     * @return ダウンロードされたファイルのパス
     */
    public String exportExcel() {
        String downloadDir = System.getProperty("java.io.tmpdir");
        String downloadPath = downloadDir + "/customers.xlsx";

        // ダウンロードディレクトリの設定
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloadDir);
        options.setExperimentalOption("prefs", prefs);

        exportExcelButton.click();

        // ダウンロード完了を待機
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(driver -> {
            File file = new File(downloadPath);
            return file.exists() && file.length() > 0;
        });

        return downloadPath;
    }

    /**
     * 取込テンプレートをダウンロード
     * @return ダウンロードされたファイルのパス
     */
    public String downloadTemplate() {
        String downloadDir = System.getProperty("java.io.tmpdir");
        String downloadPath = downloadDir + "/customer_template.xlsx";

        // ダウンロードディレクトリの設定
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloadDir);
        options.setExperimentalOption("prefs", prefs);

        downloadTemplateButton.click();

        // ダウンロード完了を待機
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(driver -> {
            File file = new File(downloadPath);
            return file.exists() && file.length() > 0;
        });

        return downloadPath;
    }

    /**
     * Excelファイルを取り込む
     * @param filePath 取り込むファイルのパス
     */
    public void importExcel(String filePath) {
        fileInput.sendKeys(filePath);
        importExcelButton.click();
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOf(successMessage),
            ExpectedConditions.visibilityOf(errorMessage)
        ));
    }

    /**
     * Excelファイルの内容を検証
     * @param filePath 検証するファイルのパス
     * @return 検証結果のマップ（行番号 -> 顧客情報）
     */
    public Map<Integer, List<String>> verifyExcelContent(String filePath) throws IOException {
        Map<Integer, List<String>> content = new HashMap<>();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // ヘッダーをスキップ
                
                List<String> rowData = new ArrayList<>();
                for (Cell cell : row) {
                    rowData.add(switch (cell.getCellType()) {
                        case STRING -> cell.getStringCellValue();
                        case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
                        default -> "";
                    });
                }
                content.put(row.getRowNum(), rowData);
            }
        }
        
        return content;
    }
}