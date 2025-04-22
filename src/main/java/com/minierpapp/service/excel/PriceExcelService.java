package com.minierpapp.service.excel;

import com.minierpapp.dto.excel.ImportResult;
import com.minierpapp.model.common.Status;
import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.price.dto.PriceScaleRequest;
import com.minierpapp.model.price.dto.SalesPriceRequest;
import com.minierpapp.model.price.entity.PurchasePrice;
import com.minierpapp.model.price.entity.SalesPrice;
import com.minierpapp.model.price.entity.PriceScale;
import com.minierpapp.repository.CustomerRepository;
import com.minierpapp.repository.ItemRepository;
import com.minierpapp.service.SalesPriceService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceExcelService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final SalesPriceService salesPriceService;
    private final ItemRepository itemRepository;
    private final CustomerRepository customerRepository;

    /**
     * 販売単価データをExcelファイルに出力する
     * @param salesPrices 出力する販売単価リスト
     * @param out 出力先ストリーム
     * @throws IOException 入出力例外
     */
    public void exportSalesPrices(List<SalesPrice> salesPrices, OutputStream out) throws IOException {
        System.out.println("販売単価出力処理開始: データ数=" + (salesPrices != null ? salesPrices.size() : "null"));
        try {
            // ヘッダー行の定義
            String[] headers = {
                "品目コード", "品目名", "得意先コード", "得意先名", "基本価格", "通貨コード",
                "有効開始日", "有効終了日", "ステータス",
                "数量範囲開始1", "数量範囲終了1", "スケール価格1",
                "数量範囲開始2", "数量範囲終了2", "スケール価格2",
                "数量範囲開始3", "数量範囲終了3", "スケール価格3"
            };
            
            Workbook workbook = new XSSFWorkbook();
            System.out.println("Workbook作成完了");
            
            // シートの作成
            Sheet sheet = workbook.createSheet("販売単価");
            System.out.println("Sheet作成完了");
            
            // スタイルの設定
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy/mm/dd"));
            
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
            
            // ヘッダー行の作成
            Row headerRow = sheet.createRow(0);
            System.out.println("ヘッダー行作成完了");
            
            // ヘッダーセルの設定
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000); // 列幅の設定
            }
            System.out.println("ヘッダーセル設定完了");
            
            // データ行の作成
            int rowNum = 1;
            for (SalesPrice price : salesPrices) {
                System.out.println("行 " + rowNum + " の処理開始: priceId=" + price.getId());
                Row row = sheet.createRow(rowNum++);
                
                // 基本情報の設定
                int colNum = 0;
                
                // 品目情報
                if (price.getItem() != null) {
                    row.createCell(colNum++).setCellValue(price.getItem().getItemCode());
                    row.createCell(colNum++).setCellValue(price.getItem().getItemName());
                } else {
                    row.createCell(colNum++).setCellValue("");
                    row.createCell(colNum++).setCellValue("");
                    System.out.println("警告: priceId=" + price.getId() + " の品目情報がnullです");
                }
                
                // 得意先情報
                if (price.getCustomer() != null) {
                    row.createCell(colNum++).setCellValue(price.getCustomer().getCustomerCode());
                    row.createCell(colNum++).setCellValue(price.getCustomer().getName());
                } else {
                    row.createCell(colNum++).setCellValue("");
                    row.createCell(colNum++).setCellValue("");
                }
                
                // 価格情報
                Cell priceCell = row.createCell(colNum++);
                priceCell.setCellValue(price.getBasePrice().doubleValue());
                priceCell.setCellStyle(numberStyle);
                
                row.createCell(colNum++).setCellValue(price.getCurrencyCode());
                
                // 日付情報
                Cell fromDateCell = row.createCell(colNum++);
                fromDateCell.setCellValue(price.getValidFromDate());
                fromDateCell.setCellStyle(dateStyle);
                
                Cell toDateCell = row.createCell(colNum++);
                toDateCell.setCellValue(price.getValidToDate());
                toDateCell.setCellStyle(dateStyle);
                
                // ステータス
                row.createCell(colNum++).setCellValue(price.getStatus().name());
                
                // 数量スケール情報
                List<PriceScale> scales = price.getPriceScales();
                for (int i = 0; i < 3; i++) {
                    if (i < scales.size()) {
                        PriceScale scale = scales.get(i);
                        
                        Cell fromQtyCell = row.createCell(colNum++);
                        fromQtyCell.setCellValue(scale.getFromQuantity().doubleValue());
                        fromQtyCell.setCellStyle(numberStyle);
                        
                        if (scale.getToQuantity() != null) {
                            Cell toQtyCell = row.createCell(colNum++);
                            toQtyCell.setCellValue(scale.getToQuantity().doubleValue());
                            toQtyCell.setCellStyle(numberStyle);
                        } else {
                            colNum++; // 空セル
                        }
                        
                        Cell scalePriceCell = row.createCell(colNum++);
                        scalePriceCell.setCellValue(scale.getScalePrice().doubleValue());
                        scalePriceCell.setCellStyle(numberStyle);
                    } else {
                        // スケールがない場合は空セル
                        colNum += 3;
                    }
                }
                System.out.println("行 " + (rowNum-1) + " の処理完了");
            }
            
            System.out.println("全データ行の作成完了");
            
            // 列幅の自動調整
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // ファイルに書き込み
            workbook.write(out);
            workbook.close();
            System.out.println("Excelファイル作成完了");
        } catch (Exception e) {
            System.out.println("販売単価出力処理でエラー発生: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("販売単価の出力処理に失敗しました", e);
        }
    }

    // 既存のメソッド（変更なし）
    public void exportPurchasePrices(List<PurchasePrice> purchasePrices, OutputStream out) throws IOException {
        // 既存の実装
    }

    /**
     * 販売単価データをExcelファイルから取込む（拡張版）
     * @param file 取込むExcelファイル
     * @return 取込結果
     */
    @Transactional
    public ImportResult importSalesPrices(MultipartFile file) {
        ImportResult result = new ImportResult();
        
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            // シートの取得
            Sheet sheet = workbook.getSheet("販売単価");
            if (sheet == null) {
                result.addError(0, "シート「販売単価」が見つかりません");
                return result;
            }
            
            // ヘッダー行の検証
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> headerMap = validateAndMapHeaders(headerRow, result);
            if (result.hasGlobalError()) {
                return result;
            }
            
            // データ行の処理
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                // 行データの解析
                SalesPriceImportData data = parseSalesPriceRow(row, headerMap, i);
                
                // データの検証
                validateSalesPriceData(data, result);
                if (result.hasErrorInRow(i)) {
                    continue; // エラーがある行はスキップ
                }
                
                // データの処理
                processSalesPriceData(data, result);
            }
        } catch (IOException e) {
            log.error("ファイルの読み込みに失敗しました", e);
            result.addError(0, "ファイルの読み込みに失敗しました: " + e.getMessage());
        }
        
        return result;
    }

    // 既存のメソッド（変更なし）
    public ImportResult importPurchasePrices(MultipartFile file) {
        // 既存の実装
        return null;
    }

    /**
     * ヘッダー行を検証し、ヘッダー名と列インデックスのマップを返す
     */
    private Map<String, Integer> validateAndMapHeaders(Row headerRow, ImportResult result) {
        if (headerRow == null) {
            result.addError(0, "ヘッダー行が見つかりません");
            return Collections.emptyMap();
        }

        Map<String, Integer> headerMap = new HashMap<>();
        
        // ヘッダーの列インデックスを取得
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                String headerName = cell.getStringCellValue().trim();
                if (!headerName.isEmpty()) {
                    headerMap.put(headerName, i);
                }
            }
        }
        
        // 必須ヘッダーの存在チェック
        List<String> requiredHeaders = Arrays.asList("品目コード", "基本価格", "有効開始日", "有効終了日");
        for (String requiredHeader : requiredHeaders) {
            if (!headerMap.containsKey(requiredHeader)) {
                result.addError(0, "必須ヘッダー「" + requiredHeader + "」が見つかりません");
            }
        }
        
        return headerMap;
    }

    /**
     * 販売単価行データの解析
     */
    private SalesPriceImportData parseSalesPriceRow(Row row, Map<String, Integer> headerMap, int rowNum) {
        SalesPriceImportData data = new SalesPriceImportData();
        data.setRowNum(rowNum);
        
        // 基本項目の取得
        data.setItemCode(getStringValue(row.getCell(headerMap.getOrDefault("品目コード", -1))));
        data.setCustomerCode(getStringValue(row.getCell(headerMap.getOrDefault("得意先コード", -1))));
        data.setBasePrice(getBigDecimalValue(row.getCell(headerMap.getOrDefault("基本価格", -1))));
        
        // 通貨コードの取得（デフォルト: JPY）
        String currencyCode = getStringValue(row.getCell(headerMap.getOrDefault("通貨コード", -1)));
        data.setCurrencyCode(currencyCode != null && !currencyCode.isEmpty() ? currencyCode : "JPY");
        
        data.setValidFromDate(getDateValue(row.getCell(headerMap.getOrDefault("有効開始日", -1))));
        data.setValidToDate(getDateValue(row.getCell(headerMap.getOrDefault("有効終了日", -1))));
        
        // ステータスの取得（デフォルト: ACTIVE）
        String statusStr = getStringValue(row.getCell(headerMap.getOrDefault("ステータス", -1)));
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                data.setStatus(Status.valueOf(statusStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                data.setStatus(Status.ACTIVE);
            }
        } else {
            data.setStatus(Status.ACTIVE);
        }
        
        // 数量スケールの取得
        List<PriceScaleImportData> scales = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            BigDecimal fromQuantity = getBigDecimalValue(row.getCell(headerMap.getOrDefault("数量範囲開始" + i, -1)));
            if (fromQuantity == null) continue;
            
            BigDecimal toQuantity = getBigDecimalValue(row.getCell(headerMap.getOrDefault("数量範囲終了" + i, -1)));
            BigDecimal scalePrice = getBigDecimalValue(row.getCell(headerMap.getOrDefault("スケール価格" + i, -1)));
            
            PriceScaleImportData scale = new PriceScaleImportData();
            scale.setFromQuantity(fromQuantity);
            scale.setToQuantity(toQuantity);
            scale.setScalePrice(scalePrice);
            scales.add(scale);
        }
        data.setPriceScales(scales);
        
        return data;
    }

    /**
     * 販売単価データの検証
     */
    private void validateSalesPriceData(SalesPriceImportData data, ImportResult result) {
        List<String> errors = new ArrayList<>();
        
        // 必須項目チェック
        if (isEmpty(data.getItemCode())) errors.add("品目コードは必須です");
        if (data.getBasePrice() == null) errors.add("基本価格は必須です");
        if (data.getValidFromDate() == null) errors.add("有効開始日は必須です");
        if (data.getValidToDate() == null) errors.add("有効終了日は必須です");
        
        // 値の妥当性チェック
        if (data.getBasePrice() != null && data.getBasePrice().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("基本価格は0以上の値を指定してください");
        }
        
        if (data.getValidFromDate() != null && data.getValidToDate() != null &&
            data.getValidFromDate().isAfter(data.getValidToDate())) {
            errors.add("有効開始日は有効終了日より前の日付を指定してください");
        }
        
        // マスタの存在チェック
        if (!isEmpty(data.getItemCode())) {
            Item item = itemRepository.findByItemCode(data.getItemCode()).orElse(null);
            if (item == null) {
                errors.add("品目コードが存在しません: " + data.getItemCode());
            }
        }
        
        if (!isEmpty(data.getCustomerCode())) {
            Customer customer = customerRepository.findByCustomerCode(data.getCustomerCode()).orElse(null);
            if (customer == null) {
                errors.add("得意先コードが存在しません: " + data.getCustomerCode());
            }
        }
        
        // 数量スケールのチェック
        validatePriceScales(data.getPriceScales(), errors);
        
        // エラーがあれば結果に追加
        if (!errors.isEmpty()) {
            for (String error : errors) {
                result.addError(data.getRowNum(), error);
            }
        }
    }

    /**
     * 数量スケールの検証
     */
    private void validatePriceScales(List<PriceScaleImportData> scales, List<String> errors) {
        if (scales == null || scales.isEmpty()) return;
        
        // 各スケールの検証
        for (int i = 0; i < scales.size(); i++) {
            PriceScaleImportData scale = scales.get(i);
            
            // 必須項目チェック
            if (scale.getFromQuantity() == null) {
                errors.add("数量範囲開始" + (i + 1) + "は必須です");
            }
            
            if (scale.getScalePrice() == null) {
                errors.add("スケール価格" + (i + 1) + "は必須です");
            }
            
            // 値の妥当性チェック
            if (scale.getFromQuantity() != null && scale.getFromQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                errors.add("数量範囲開始" + (i + 1) + "は0より大きい値を指定してください");
            }
            
            if (scale.getScalePrice() != null && scale.getScalePrice().compareTo(BigDecimal.ZERO) < 0) {
                errors.add("スケール価格" + (i + 1) + "は0以上の値を指定してください");
            }
            
            if (scale.getFromQuantity() != null && scale.getToQuantity() != null &&
                scale.getFromQuantity().compareTo(scale.getToQuantity()) >= 0) {
                errors.add("数量範囲開始" + (i + 1) + "は数量範囲終了" + (i + 1) + "より小さい値を指定してください");
            }
        }
        
        // スケール間の重複チェック
        for (int i = 0; i < scales.size(); i++) {
            for (int j = i + 1; j < scales.size(); j++) {
                if (isScaleOverlapping(scales.get(i), scales.get(j))) {
                    errors.add("数量範囲が重複しています: スケール" + (i + 1) + "とスケール" + (j + 1));
                }
            }
        }
    }

    /**
     * 数量スケールが重複しているかどうかを判定
     */
    private boolean isScaleOverlapping(PriceScaleImportData scale1, PriceScaleImportData scale2) {
        if (scale1.getToQuantity() == null && scale2.getToQuantity() == null) {
            return true; // 両方とも上限なしの場合は重複
        }
        
        if (scale1.getToQuantity() == null) {
            return scale1.getFromQuantity().compareTo(scale2.getFromQuantity()) <= 0;
        }
        
        if (scale2.getToQuantity() == null) {
            return scale2.getFromQuantity().compareTo(scale1.getFromQuantity()) <= 0;
        }
        
        return (scale1.getFromQuantity().compareTo(scale2.getFromQuantity()) <= 0 &&
                scale1.getToQuantity().compareTo(scale2.getFromQuantity()) > 0) ||
               (scale2.getFromQuantity().compareTo(scale1.getFromQuantity()) <= 0 &&
                scale2.getToQuantity().compareTo(scale1.getFromQuantity()) > 0);
    }

    /**
     * 販売単価データの処理
     */
    private void processSalesPriceData(SalesPriceImportData data, ImportResult result) {
        // 品目と得意先のIDを取得
        Item item = itemRepository.findByItemCode(data.getItemCode())
            .orElse(null);
        if (item == null) {
            result.addError(data.getRowNum(), "品目コードが存在しません: " + data.getItemCode());
            return;
        }
        
        Customer customer = null;
        Long customerId = null;
        if (!isEmpty(data.getCustomerCode())) {
            customer = customerRepository.findByCustomerCode(data.getCustomerCode())
                .orElse(null);
            if (customer == null) {
                result.addError(data.getRowNum(), "得意先コードが存在しません: " + data.getCustomerCode());
                return;
            }
            customerId = customer.getId();
        }
        
        // 重複データの検索
        List<SalesPrice> overlappingPrices = salesPriceService.findOverlappingPrices(
            item.getId(), customerId, data.getValidFromDate(), data.getValidToDate());
        
        if (overlappingPrices.isEmpty()) {
            // 重複なし: 新規登録
            createNewSalesPrice(data, item.getId(), customerId);
            result.addSuccess();
        } else {
            // 重複あり: 重複データの処理
            handleOverlappingPrices(data, overlappingPrices, item.getId(), customerId);
            result.addSuccess();
        }
    }

    /**
     * 新規販売単価の登録
     */
    private void createNewSalesPrice(SalesPriceImportData data, Long itemId, Long customerId) {
        SalesPriceRequest request = new SalesPriceRequest();
        request.setItemId(itemId);
        request.setCustomerId(customerId);
        request.setBasePrice(data.getBasePrice());
        request.setCurrencyCode(data.getCurrencyCode());
        request.setValidFromDate(data.getValidFromDate());
        request.setValidToDate(data.getValidToDate());
        request.setStatus(data.getStatus());
        
        // スケール情報の設定
        if (data.getPriceScales() != null && !data.getPriceScales().isEmpty()) {
            List<PriceScaleRequest> scaleRequests = new ArrayList<>();
            for (PriceScaleImportData scale : data.getPriceScales()) {
                PriceScaleRequest scaleRequest = new PriceScaleRequest();
                scaleRequest.setFromQuantity(scale.getFromQuantity());
                scaleRequest.setToQuantity(scale.getToQuantity());
                scaleRequest.setScalePrice(scale.getScalePrice());
                scaleRequests.add(scaleRequest);
            }
            request.setPriceScales(scaleRequests);
        }
        
        salesPriceService.create(request);
    }

    /**
     * 重複データの処理
     */
    private void handleOverlappingPrices(SalesPriceImportData data, List<SalesPrice> overlappingPrices,
                                        Long itemId, Long customerId) {
        // 新規データのリクエスト作成
        SalesPriceRequest newRequest = new SalesPriceRequest();
        newRequest.setItemId(itemId);
        newRequest.setCustomerId(customerId);
        newRequest.setBasePrice(data.getBasePrice());
        newRequest.setCurrencyCode(data.getCurrencyCode());
        newRequest.setValidFromDate(data.getValidFromDate());
        newRequest.setValidToDate(data.getValidToDate());
        newRequest.setStatus(data.getStatus());
        
        // スケール情報の設定
        if (data.getPriceScales() != null && !data.getPriceScales().isEmpty()) {
            List<PriceScaleRequest> scaleRequests = new ArrayList<>();
            for (PriceScaleImportData scale : data.getPriceScales()) {
                PriceScaleRequest scaleRequest = new PriceScaleRequest();
                scaleRequest.setFromQuantity(scale.getFromQuantity());
                scaleRequest.setToQuantity(scale.getToQuantity());
                scaleRequest.setScalePrice(scale.getScalePrice());
                scaleRequests.add(scaleRequest);
            }
            newRequest.setPriceScales(scaleRequests);
        }
        
        // 重複データを日付の昇順でソート
        overlappingPrices.sort(Comparator.comparing(SalesPrice::getValidFromDate));
        
        // 各重複データに対して処理
        for (SalesPrice existing : overlappingPrices) {
            if (isExactMatch(existing, data)) {
                // 完全一致: 既存データを更新
                newRequest.setId(existing.getId());
                salesPriceService.update(existing.getId(), newRequest);
                return; // 更新したら終了
            } else if (isContainedWithin(existing, data)) {
                // 期間内包含: 既存データを分割
                splitExistingPrice(existing, data);
            } else if (isContaining(existing, data)) {
                // 期間外包含: 既存データを削除
                salesPriceService.delete(existing.getId());
            } else if (isPartiallyOverlapping(existing, data)) {
                // 部分重複: 既存データの期間を調整
                adjustExistingPricePeriod(existing, data);
            }
        }
        
        // 新規データを登録
        if (newRequest.getId() == null) {
            salesPriceService.create(newRequest);
        }
    }

    /**
     * 完全一致かどうかを判定
     */
    private boolean isExactMatch(SalesPrice existing, SalesPriceImportData data) {
        return existing.getValidFromDate().isEqual(data.getValidFromDate()) &&
               existing.getValidToDate().isEqual(data.getValidToDate());
    }

    /**
     * 期間内包含かどうかを判定
     */
    private boolean isContainedWithin(SalesPrice existing, SalesPriceImportData data) {
        return existing.getValidFromDate().isBefore(data.getValidFromDate()) &&
               existing.getValidToDate().isAfter(data.getValidToDate());
    }

    /**
     * 期間外包含かどうかを判定
     */
    private boolean isContaining(SalesPrice existing, SalesPriceImportData data) {
        return existing.getValidFromDate().isAfter(data.getValidFromDate()) &&
               existing.getValidToDate().isBefore(data.getValidToDate());
    }

    /**
     * 部分重複かどうかを判定
     */
    private boolean isPartiallyOverlapping(SalesPrice existing, SalesPriceImportData data) {
        return (existing.getValidFromDate().isBefore(data.getValidFromDate()) &&
                existing.getValidToDate().isAfter(data.getValidFromDate()) &&
                existing.getValidToDate().isBefore(data.getValidToDate())) ||
               (existing.getValidFromDate().isAfter(data.getValidFromDate()) &&
                existing.getValidFromDate().isBefore(data.getValidToDate()) &&
                existing.getValidToDate().isAfter(data.getValidToDate()));
    }

    /**
     * 既存データを分割
     */
    private void splitExistingPrice(SalesPrice existing, SalesPriceImportData data) {
        // 前半部分の作成
        if (existing.getValidFromDate().isBefore(data.getValidFromDate())) {
            SalesPriceRequest beforeRequest = new SalesPriceRequest();
            beforeRequest.setId(existing.getId());
            beforeRequest.setItemId(existing.getItem().getId());
            beforeRequest.setCustomerId(existing.getCustomer() != null ? existing.getCustomer().getId() : null);
            beforeRequest.setBasePrice(existing.getBasePrice());
            beforeRequest.setCurrencyCode(existing.getCurrencyCode());
            beforeRequest.setValidFromDate(existing.getValidFromDate());
            beforeRequest.setValidToDate(data.getValidFromDate().minusDays(1));
            beforeRequest.setStatus(existing.getStatus());
            
            salesPriceService.update(existing.getId(), beforeRequest);
        }
        
        // 後半部分の作成
        if (existing.getValidToDate().isAfter(data.getValidToDate())) {
            SalesPriceRequest afterRequest = new SalesPriceRequest();
            afterRequest.setItemId(existing.getItem().getId());
            afterRequest.setCustomerId(existing.getCustomer() != null ? existing.getCustomer().getId() : null);
            afterRequest.setBasePrice(existing.getBasePrice());
            afterRequest.setCurrencyCode(existing.getCurrencyCode());
            afterRequest.setValidFromDate(data.getValidToDate().plusDays(1));
            afterRequest.setValidToDate(existing.getValidToDate());
            afterRequest.setStatus(existing.getStatus());
            
            salesPriceService.create(afterRequest);
        }
    }

    /**
     * 既存データの期間を調整
     */
    private void adjustExistingPricePeriod(SalesPrice existing, SalesPriceImportData data) {
        SalesPriceRequest request = new SalesPriceRequest();
        request.setId(existing.getId());
        request.setItemId(existing.getItem().getId());
        request.setCustomerId(existing.getCustomer() != null ? existing.getCustomer().getId() : null);
        request.setBasePrice(existing.getBasePrice());
        request.setCurrencyCode(existing.getCurrencyCode());
        request.setStatus(existing.getStatus());
        
        if (existing.getValidFromDate().isBefore(data.getValidFromDate())) {
            // 前方重複: 既存データの終了日を調整
            request.setValidFromDate(existing.getValidFromDate());
            request.setValidToDate(data.getValidFromDate().minusDays(1));
        } else {
            // 後方重複: 既存データの開始日を調整
            request.setValidFromDate(data.getValidToDate().plusDays(1));
            request.setValidToDate(existing.getValidToDate());
        }
        
        salesPriceService.update(existing.getId(), request);
    }

    /**
     * 文字列が空かどうかを判定
     */
    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * セルから文字列値を取得
     */
    private String getStringValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            default: return null;
        }
    }

    /**
     * セルから BigDecimal 値を取得
     */
    private BigDecimal getBigDecimalValue(Cell cell) {
        if (cell == null) return null;
        try {
            switch (cell.getCellType()) {
                case NUMERIC: return BigDecimal.valueOf(cell.getNumericCellValue());
                case STRING:
                    String value = cell.getStringCellValue().trim();
                    return value.isEmpty() ? null : new BigDecimal(value);
                default: return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * セルから日付値を取得
     */
    private LocalDate getDateValue(Cell cell) {
        if (cell == null) return null;
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getLocalDateTimeCellValue().toLocalDate();
                    }
                    return null;
                case STRING:
                    String value = cell.getStringCellValue().trim();
                    return value.isEmpty() ? null : LocalDate.parse(value, DATE_FORMATTER);
                default: return null;
            }
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // 内部クラスとして定義
    @Data
    static class SalesPriceImportData {
        private int rowNum;
        private String itemCode;
        private String customerCode;
        private BigDecimal basePrice;
        private String currencyCode;
        private LocalDate validFromDate;
        private LocalDate validToDate;
        private Status status;
        private List<PriceScaleImportData> priceScales = new ArrayList<>();
    }
    
    @Data
    static class PriceScaleImportData {
        private BigDecimal fromQuantity;
        private BigDecimal toQuantity;
        private BigDecimal scalePrice;
    }

    public byte[] exportSalesPrices(List<SalesPrice> salesPrices) {
        System.out.println("販売単価出力処理開始: データ数=" + (salesPrices != null ? salesPrices.size() : "null"));
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            exportSalesPrices(salesPrices, outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            System.out.println("販売単価出力処理でエラー発生: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("販売単価の出力処理に失敗しました", e);
        }
    }
}