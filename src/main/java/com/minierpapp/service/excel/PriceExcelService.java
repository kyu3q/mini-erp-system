package com.minierpapp.service.excel;

import com.minierpapp.dto.excel.ImportResult;
import com.minierpapp.dto.excel.PriceExcelRow;
import com.minierpapp.model.common.Status;
import com.minierpapp.model.price.PriceCondition;
import com.minierpapp.model.price.PriceScale;
import com.minierpapp.service.CustomerService;
import com.minierpapp.service.ItemService;
import com.minierpapp.service.PriceService;
import com.minierpapp.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceExcelService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final String[] SALES_HEADERS = {
            "品目コード", "品目名", "得意先コード", "得意先名", "基本価格", "通貨",
            "有効開始日", "有効終了日", "ステータス",
            "スケール1_開始数量", "スケール1_終了数量", "スケール1_価格",
            "スケール2_開始数量", "スケール2_終了数量", "スケール2_価格",
            "スケール3_開始数量", "スケール3_終了数量", "スケール3_価格"
    };
    private static final String[] PURCHASE_HEADERS = {
            "品目コード", "品目名", "仕入先コード", "仕入先名", "得意先コード", "得意先名",
            "基本価格", "通貨", "有効開始日", "有効終了日", "ステータス",
            "スケール1_開始数量", "スケール1_終了数量", "スケール1_価格",
            "スケール2_開始数量", "スケール2_終了数量", "スケール2_価格",
            "スケール3_開始数量", "スケール3_終了数量", "スケール3_価格"
    };

    private final PriceService priceService;
    private final ItemService itemService;
    private final CustomerService customerService;
    private final SupplierService supplierService;

    public void exportSalesPrices(List<PriceCondition> conditions, OutputStream out) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("販売単価");
            
            // スタイルの設定
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            
            // ヘッダー行の作成
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < SALES_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(SALES_HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }

            // データ行の作成
            int rowNum = 1;
            for (PriceCondition condition : conditions) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                // 基本情報
                createCell(row, colNum++, condition.getItemCode());
                createCell(row, colNum++, condition.getItem().getItemName());
                createCell(row, colNum++, condition.getCustomerCode());
                createCell(row, colNum++, condition.getCustomer() != null ? condition.getCustomer().getName() : "");
                createCell(row, colNum++, condition.getBasePrice(), numberStyle);
                createCell(row, colNum++, condition.getCurrencyCode());
                createCell(row, colNum++, condition.getValidFromDate(), dateStyle);
                createCell(row, colNum++, condition.getValidToDate(), dateStyle);
                createCell(row, colNum++, condition.getStatus().name());

                // スケール価格
                for (PriceScale scale : condition.getPriceScales()) {
                    createCell(row, colNum++, scale.getFromQuantity(), numberStyle);
                    createCell(row, colNum++, scale.getToQuantity(), numberStyle);
                    createCell(row, colNum++, scale.getScalePrice(), numberStyle);
                }
            }

            workbook.write(out);
        }
    }

    public void exportPurchasePrices(List<PriceCondition> conditions, OutputStream out) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("購買単価");
            
            // スタイルの設定
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            
            // ヘッダー行の作成
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < PURCHASE_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(PURCHASE_HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }

            // データ行の作成
            int rowNum = 1;
            for (PriceCondition condition : conditions) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                // 基本情報
                createCell(row, colNum++, condition.getItemCode());
                createCell(row, colNum++, condition.getItem().getItemName());
                createCell(row, colNum++, condition.getSupplierCode());
                createCell(row, colNum++, condition.getSupplier() != null ? condition.getSupplier().getName() : "");
                createCell(row, colNum++, condition.getCustomerCode());
                createCell(row, colNum++, condition.getCustomer() != null ? condition.getCustomer().getName() : "");
                createCell(row, colNum++, condition.getBasePrice(), numberStyle);
                createCell(row, colNum++, condition.getCurrencyCode());
                createCell(row, colNum++, condition.getValidFromDate(), dateStyle);
                createCell(row, colNum++, condition.getValidToDate(), dateStyle);
                createCell(row, colNum++, condition.getStatus().name());

                // スケール価格
                for (PriceScale scale : condition.getPriceScales()) {
                    createCell(row, colNum++, scale.getFromQuantity(), numberStyle);
                    createCell(row, colNum++, scale.getToQuantity(), numberStyle);
                    createCell(row, colNum++, scale.getScalePrice(), numberStyle);
                }
            }

            workbook.write(out);
        }
    }

    public ImportResult importSalesPrices(MultipartFile file) {
        ImportResult result = new ImportResult();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            validateHeaders(sheet.getRow(0), SALES_HEADERS);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    PriceExcelRow priceRow = readSalesRow(row);
                    validateSalesRow(priceRow);
                    PriceCondition condition = convertToPriceCondition(priceRow, "SALES");
                    priceService.save(condition);
                    result.addSuccess();
                } catch (Exception e) {
                    result.addError(i + 1, e.getMessage());
                }
            }
        } catch (Exception e) {
            result.addError(0, "ファイルの読み込みに失敗しました: " + e.getMessage());
        }

        return result;
    }

    public ImportResult importPurchasePrices(MultipartFile file) {
        ImportResult result = new ImportResult();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            validateHeaders(sheet.getRow(0), PURCHASE_HEADERS);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    PriceExcelRow priceRow = readPurchaseRow(row);
                    validatePurchaseRow(priceRow);
                    PriceCondition condition = convertToPriceCondition(priceRow, "PURCHASE");
                    priceService.save(condition);
                    result.addSuccess();
                } catch (Exception e) {
                    result.addError(i + 1, e.getMessage());
                }
            }
        } catch (Exception e) {
            result.addError(0, "ファイルの読み込みに失敗しました: " + e.getMessage());
        }

        return result;
    }

    private void validateHeaders(Row headerRow, String[] expectedHeaders) {
        if (headerRow == null) {
            throw new IllegalArgumentException("ヘッダー行が見つかりません");
        }

        for (int i = 0; i < expectedHeaders.length; i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null || !expectedHeaders[i].equals(cell.getStringCellValue())) {
                throw new IllegalArgumentException("ヘッダーの形式が不正です: " + expectedHeaders[i]);
            }
        }
    }

    private PriceExcelRow readSalesRow(Row row) {
        PriceExcelRow priceRow = new PriceExcelRow();
        priceRow.setRowNum(row.getRowNum() + 1);
        int colNum = 0;

        priceRow.setItemCode(getStringValue(row.getCell(colNum++)));
        priceRow.setItemName(getStringValue(row.getCell(colNum++)));
        priceRow.setCustomerCode(getStringValue(row.getCell(colNum++)));
        priceRow.setCustomerName(getStringValue(row.getCell(colNum++)));
        priceRow.setBasePrice(getBigDecimalValue(row.getCell(colNum++)));
        priceRow.setCurrencyCode(getStringValue(row.getCell(colNum++)));
        priceRow.setValidFromDate(getDateValue(row.getCell(colNum++)));
        priceRow.setValidToDate(getDateValue(row.getCell(colNum++)));
        priceRow.setStatus(Status.valueOf(getStringValue(row.getCell(colNum++))));

        // スケール価格の読み込み
        List<PriceExcelRow.PriceScaleExcelRow> scales = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            BigDecimal fromQuantity = getBigDecimalValue(row.getCell(colNum++));
            BigDecimal toQuantity = getBigDecimalValue(row.getCell(colNum++));
            BigDecimal scalePrice = getBigDecimalValue(row.getCell(colNum++));

            if (fromQuantity != null || toQuantity != null || scalePrice != null) {
                PriceExcelRow.PriceScaleExcelRow scale = new PriceExcelRow.PriceScaleExcelRow();
                scale.setFromQuantity(fromQuantity);
                scale.setToQuantity(toQuantity);
                scale.setScalePrice(scalePrice);
                scales.add(scale);
            }
        }
        priceRow.setScales(scales);

        return priceRow;
    }

    private PriceExcelRow readPurchaseRow(Row row) {
        PriceExcelRow priceRow = new PriceExcelRow();
        priceRow.setRowNum(row.getRowNum() + 1);
        int colNum = 0;

        priceRow.setItemCode(getStringValue(row.getCell(colNum++)));
        priceRow.setItemName(getStringValue(row.getCell(colNum++)));
        priceRow.setSupplierCode(getStringValue(row.getCell(colNum++)));
        priceRow.setSupplierName(getStringValue(row.getCell(colNum++)));
        priceRow.setCustomerCode(getStringValue(row.getCell(colNum++)));
        priceRow.setCustomerName(getStringValue(row.getCell(colNum++)));
        priceRow.setBasePrice(getBigDecimalValue(row.getCell(colNum++)));
        priceRow.setCurrencyCode(getStringValue(row.getCell(colNum++)));
        priceRow.setValidFromDate(getDateValue(row.getCell(colNum++)));
        priceRow.setValidToDate(getDateValue(row.getCell(colNum++)));
        priceRow.setStatus(Status.valueOf(getStringValue(row.getCell(colNum++))));

        // スケール価格の読み込み
        List<PriceExcelRow.PriceScaleExcelRow> scales = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            BigDecimal fromQuantity = getBigDecimalValue(row.getCell(colNum++));
            BigDecimal toQuantity = getBigDecimalValue(row.getCell(colNum++));
            BigDecimal scalePrice = getBigDecimalValue(row.getCell(colNum++));

            if (fromQuantity != null || toQuantity != null || scalePrice != null) {
                PriceExcelRow.PriceScaleExcelRow scale = new PriceExcelRow.PriceScaleExcelRow();
                scale.setFromQuantity(fromQuantity);
                scale.setToQuantity(toQuantity);
                scale.setScalePrice(scalePrice);
                scales.add(scale);
            }
        }
        priceRow.setScales(scales);

        return priceRow;
    }

    private void validateSalesRow(PriceExcelRow row) {
        List<String> errors = new ArrayList<>();

        // 必須項目のチェック
        if (isEmpty(row.getItemCode())) errors.add("品目コードは必須です");
        if (row.getBasePrice() == null) errors.add("基本価格は必須です");
        if (isEmpty(row.getCurrencyCode())) errors.add("通貨コードは必須です");
        if (row.getValidFromDate() == null) errors.add("有効開始日は必須です");
        if (row.getValidToDate() == null) errors.add("有効終了日は必須です");
        if (row.getStatus() == null) errors.add("ステータスは必須です");

        // 品目の存在チェック
        if (!isEmpty(row.getItemCode()) && !itemService.existsByCode(row.getItemCode())) {
            errors.add("品目コードが存在しません: " + row.getItemCode());
        }

        // 得意先の存在チェック
        if (!isEmpty(row.getCustomerCode()) && !customerService.existsByCode(row.getCustomerCode())) {
            errors.add("得意先コードが存在しません: " + row.getCustomerCode());
        }

        // 日付の妥当性チェック
        if (row.getValidFromDate() != null && row.getValidToDate() != null &&
            row.getValidFromDate().isAfter(row.getValidToDate())) {
            errors.add("有効開始日は有効終了日以前である必要があります");
        }

        // スケール価格のチェック
        validateScales(row.getScales(), errors);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }
    }

    private void validatePurchaseRow(PriceExcelRow row) {
        List<String> errors = new ArrayList<>();

        // 必須項目のチェック
        if (isEmpty(row.getItemCode())) errors.add("品目コードは必須です");
        if (isEmpty(row.getSupplierCode())) errors.add("仕入先コードは必須です");
        if (row.getBasePrice() == null) errors.add("基本価格は必須です");
        if (isEmpty(row.getCurrencyCode())) errors.add("通貨コードは必須です");
        if (row.getValidFromDate() == null) errors.add("有効開始日は必須です");
        if (row.getValidToDate() == null) errors.add("有効終了日は必須です");
        if (row.getStatus() == null) errors.add("ステータスは必須です");

        // マスタの存在チェック
        if (!isEmpty(row.getItemCode()) && !itemService.existsByCode(row.getItemCode())) {
            errors.add("品目コードが存在しません: " + row.getItemCode());
        }
        if (!isEmpty(row.getSupplierCode()) && !supplierService.existsByCode(row.getSupplierCode())) {
            errors.add("仕入先コードが存在しません: " + row.getSupplierCode());
        }
        if (!isEmpty(row.getCustomerCode()) && !customerService.existsByCode(row.getCustomerCode())) {
            errors.add("得意先コードが存在しません: " + row.getCustomerCode());
        }

        // 日付の妥当性チェック
        if (row.getValidFromDate() != null && row.getValidToDate() != null &&
            row.getValidFromDate().isAfter(row.getValidToDate())) {
            errors.add("有効開始日は有効終了日以前である必要があります");
        }

        // スケール価格のチェック
        validateScales(row.getScales(), errors);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }
    }

    private void validateScales(List<PriceExcelRow.PriceScaleExcelRow> scales, List<String> errors) {
        if (scales.isEmpty()) return;

        BigDecimal lastToQuantity = null;
        for (int i = 0; i < scales.size(); i++) {
            PriceExcelRow.PriceScaleExcelRow scale = scales.get(i);

            // 必須項目チェック
            if (scale.getFromQuantity() == null) {
                errors.add("スケール" + (i + 1) + "の開始数量は必須です");
            }
            if (scale.getScalePrice() == null) {
                errors.add("スケール" + (i + 1) + "の価格は必須です");
            }

            // 数量の連続性チェック
            if (lastToQuantity != null && scale.getFromQuantity() != null &&
                lastToQuantity.compareTo(scale.getFromQuantity()) >= 0) {
                errors.add("スケール" + (i + 1) + "の開始数量は前のスケールの終了数量より大きい必要があります");
            }

            lastToQuantity = scale.getToQuantity();
        }
    }

    private PriceCondition convertToPriceCondition(PriceExcelRow row, String priceType) {
        PriceCondition condition = new PriceCondition();
        condition.setPriceType(priceType);
        condition.setItemCode(row.getItemCode());
        condition.setItem(itemService.findByCode(row.getItemCode()));
        condition.setCustomerCode(row.getCustomerCode());
        if (!isEmpty(row.getCustomerCode())) {
            condition.setCustomer(customerService.findByCode(row.getCustomerCode()));
        }
        condition.setSupplierCode(row.getSupplierCode());
        if (!isEmpty(row.getSupplierCode())) {
            condition.setSupplier(supplierService.findByCode(row.getSupplierCode()));
        }
        condition.setBasePrice(row.getBasePrice());
        condition.setCurrencyCode(row.getCurrencyCode());
        condition.setValidFromDate(row.getValidFromDate());
        condition.setValidToDate(row.getValidToDate());
        condition.setStatus(row.getStatus());

        // スケール価格の設定
        List<PriceScale> scales = new ArrayList<>();
        for (PriceExcelRow.PriceScaleExcelRow scaleRow : row.getScales()) {
            PriceScale scale = new PriceScale();
            scale.setFromQuantity(scaleRow.getFromQuantity());
            scale.setToQuantity(scaleRow.getToQuantity());
            scale.setScalePrice(scaleRow.getScalePrice());
            scale.setPriceCondition(condition);
            scales.add(scale);
        }
        condition.setPriceScales(scales);

        return condition;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("yyyy/mm/dd"));
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    private void createCell(Row row, int colNum, String value) {
        if (value != null) {
            row.createCell(colNum).setCellValue(value);
        }
    }

    private void createCell(Row row, int colNum, BigDecimal value, CellStyle style) {
        if (value != null) {
            Cell cell = row.createCell(colNum);
            cell.setCellValue(value.doubleValue());
            cell.setCellStyle(style);
        }
    }

    private void createCell(Row row, int colNum, LocalDate value, CellStyle style) {
        if (value != null) {
            Cell cell = row.createCell(colNum);
            cell.setCellValue(value);
            cell.setCellStyle(style);
        }
    }

    private String getStringValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            default: return null;
        }
    }

    private BigDecimal getBigDecimalValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case NUMERIC: return BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING:
                try {
                    return new BigDecimal(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return null;
                }
            default: return null;
        }
    }

    private LocalDate getDateValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate();
                }
                return null;
            case STRING:
                try {
                    return LocalDate.parse(cell.getStringCellValue(), DATE_FORMATTER);
                } catch (DateTimeParseException e) {
                    return null;
                }
            default: return null;
        }
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}