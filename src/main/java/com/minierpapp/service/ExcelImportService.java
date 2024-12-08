package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.common.Status;
import com.minierpapp.model.product.Product;
import com.minierpapp.model.product.dto.ProductRequest;
import com.minierpapp.model.warehouse.dto.WarehouseDto;
import com.minierpapp.model.warehouse.dto.WarehouseRequest;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final ProductService productService;

    public List<String> importProducts(MultipartFile file) throws IOException {
        List<String> errors = new ArrayList<>();
        int rowNum = 1; // ヘッダー行をスキップするため1から開始

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // ヘッダー行をスキップ
                rowNum = row.getRowNum() + 1;

                try {
                    ProductRequest request = readProductFromRow(row);
                    if (request != null) {
                        // 商品コードで既存の商品を検索
                        try {
                            Product existingProduct = productService.findByProductCode(request.getProductCode());
                            // 既存の商品が見つかった場合は更新、見つからない場合は新規作成
                            if (existingProduct != null) {
                                productService.update(existingProduct.getId(), request);
                            } else {
                                productService.create(request);
                            }
                        } catch (jakarta.persistence.EntityNotFoundException e) {
                            // 商品が見つからない場合は新規作成
                            productService.create(request);
                        }
                    }
                } catch (Exception e) {
                    errors.add(String.format("%d行目: %s", rowNum, e.getMessage()));
                }
            }
        }

        return errors;
    }

    private ProductRequest readProductFromRow(Row row) {
        // すべてのセルが空の場合はnullを返す
        if (isEmptyRow(row)) {
            return null;
        }

        ProductRequest request = new ProductRequest();

        // 商品コード（必須）
        String productCode = getStringCellValue(row.getCell(0));
        if (productCode == null || productCode.trim().isEmpty()) {
            throw new IllegalArgumentException("商品コードは必須です");
        }
        request.setProductCode(productCode.trim());

        // 商品名（必須）
        String productName = getStringCellValue(row.getCell(1));
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("商品名は必須です");
        }
        request.setProductName(productName.trim());

        // 単位（必須）
        String unit = getStringCellValue(row.getCell(2));
        if (unit == null || unit.trim().isEmpty()) {
            throw new IllegalArgumentException("単位は必須です");
        }
        request.setUnit(unit.trim());

        // ステータス（必須）
        String status = getStringCellValue(row.getCell(3));
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("ステータスは必須です");
        }
        if ("有効".equals(status.trim())) {
            request.setStatus(Status.ACTIVE);
        } else if ("無効".equals(status.trim())) {
            request.setStatus(Status.INACTIVE);
        } else {
            throw new IllegalArgumentException("ステータスは「有効」または「無効」を指定してください");
        }

        // 最小在庫数（任意）
        Cell minStockCell = row.getCell(4);
        if (minStockCell != null) {
            try {
                request.setMinimumStock((int) minStockCell.getNumericCellValue());
            } catch (IllegalStateException e) {
                throw new IllegalArgumentException("最小在庫数は数値を指定してください");
            }
        }

        // 最大在庫数（任意）
        Cell maxStockCell = row.getCell(5);
        if (maxStockCell != null) {
            try {
                request.setMaximumStock((int) maxStockCell.getNumericCellValue());
            } catch (IllegalStateException e) {
                throw new IllegalArgumentException("最大在庫数は数値を指定してください");
            }
        }

        // 発注点（任意）
        Cell reorderPointCell = row.getCell(6);
        if (reorderPointCell != null) {
            try {
                request.setReorderPoint((int) reorderPointCell.getNumericCellValue());
            } catch (IllegalStateException e) {
                throw new IllegalArgumentException("発注点は数値を指定してください");
            }
        }

        return request;
    }

    private boolean isEmptyRow(Row row) {
        if (row == null) return true;

        for (int i = 0; i < 7; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    public byte[] createImportTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("商品登録");

            // ヘッダースタイルの設定
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // ヘッダー行の作成
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "商品コード（必須）",
                "商品名（必須）",
                "単位（必須）",
                "ステータス（必須：有効/無効）",
                "最小在庫数",
                "最大在庫数",
                "発注点"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256); // 20文字分の幅
            }

            // サンプルデータの追加
            Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("SAMPLE-001");
            sampleRow.createCell(1).setCellValue("サンプル商品");
            sampleRow.createCell(2).setCellValue("個");
            sampleRow.createCell(3).setCellValue("有効");
            sampleRow.createCell(4).setCellValue(10);
            sampleRow.createCell(5).setCellValue(100);
            sampleRow.createCell(6).setCellValue(30);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}