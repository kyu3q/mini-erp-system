package com.minierpapp.service;

import com.minierpapp.model.item.Item;
import com.minierpapp.model.warehouse.Warehouse;
import com.minierpapp.model.price.dto.SalesPriceSearchCriteria;
import com.minierpapp.model.price.entity.SalesPrice;
import com.minierpapp.repository.WarehouseRepository;
import com.minierpapp.service.excel.PriceExcelService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final WarehouseRepository warehouseRepository;
    private final SalesPriceService salesPriceService;
    private final PriceExcelService priceExcelService;

    public byte[] exportWarehouses() throws IOException {
        List<Warehouse> warehouses = warehouseRepository.findAll();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("倉庫一覧");

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

            // データセルのスタイル設定
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // 数値セルのスタイル設定
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.cloneStyleFrom(dataStyle);
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);

            // ヘッダー行の作成
            Row headerRow = sheet.createRow(0);
            String[] headers = {"倉庫コード", "倉庫名", "住所", "収容能力", "ステータス", "説明"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 15 * 256); // 15文字分の幅
            }

            // データ行の作成
            int rowNum = 1;
            for (Warehouse warehouse : warehouses) {
                Row row = sheet.createRow(rowNum++);
                
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(warehouse.getWarehouseCode());
                cell0.setCellStyle(dataStyle);

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(warehouse.getName());
                cell1.setCellStyle(dataStyle);

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(warehouse.getAddress() != null ? warehouse.getAddress() : "");
                cell2.setCellStyle(dataStyle);

                Cell cell3 = row.createCell(3);
                if (warehouse.getCapacity() != null) {
                    cell3.setCellValue(warehouse.getCapacity());
                    cell3.setCellStyle(numberStyle);
                } else {
                    cell3.setCellValue("");
                    cell3.setCellStyle(dataStyle);
                }

                Cell cell4 = row.createCell(4);
                cell4.setCellValue(warehouse.getStatus().name().equals("ACTIVE") ? "有効" : "無効");
                cell4.setCellStyle(dataStyle);

                Cell cell5 = row.createCell(5);
                cell5.setCellValue(warehouse.getDescription() != null ? warehouse.getDescription() : "");
                cell5.setCellStyle(dataStyle);
            }

            // Excelファイルをバイト配列に変換
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportItemsToExcel(List<Item> items) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("商品一覧");

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

            // データセルのスタイル設定
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // 数値セルのスタイル設定
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.cloneStyleFrom(dataStyle);
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);

            // ヘッダー行の作成
            Row headerRow = sheet.createRow(0);
            String[] headers = {"商品コード", "商品名", "単位", "ステータス", "最小在庫数", "最大在庫数", "発注点"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 15 * 256); // 15文字分の幅
            }

            // データ行の作成
            int rowNum = 1;
            for (Item item : items) {
                Row row = sheet.createRow(rowNum++);
                
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(item.getItemCode());
                cell0.setCellStyle(dataStyle);

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(item.getItemName());
                cell1.setCellStyle(dataStyle);

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(item.getUnit());
                cell2.setCellStyle(dataStyle);

                Cell cell3 = row.createCell(3);
                cell3.setCellValue(item.getStatus().name().equals("ACTIVE") ? "有効" : "無効");
                cell3.setCellStyle(dataStyle);

                Cell cell4 = row.createCell(4);
                if (item.getMinimumStock() != null) {
                    cell4.setCellValue(item.getMinimumStock());
                } else {
                    cell4.setCellValue("");
                }
                cell4.setCellStyle(numberStyle);

                Cell cell5 = row.createCell(5);
                if (item.getMaximumStock() != null) {
                    cell5.setCellValue(item.getMaximumStock());
                } else {
                    cell5.setCellValue("");
                }
                cell5.setCellStyle(numberStyle);

                Cell cell6 = row.createCell(6);
                if (item.getReorderPoint() != null) {
                    cell6.setCellValue(item.getReorderPoint());
                } else {
                    cell6.setCellValue("");
                }
                cell6.setCellStyle(numberStyle);
            }

            // Excelファイルをバイト配列に変換
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public void exportItems(HttpServletResponse response, String searchParam1, String searchParam2) throws IOException {
        // エクスポートロジックの実装
        // 例：
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=items.xlsx");
        // ... エクセルファイル生成ロジック
    }

    public void downloadItemTemplate(HttpServletResponse response) throws IOException {
        // テンプレートダウンロードロジックの実装
    }

    public byte[] exportSalesPrices(SalesPriceSearchCriteria criteria) {
        System.out.println("販売単価出力サービス開始: 検索条件=" + criteria);
        try {
            List<SalesPrice> salesPrices = salesPriceService.searchSalesPricesForExport(criteria);
            System.out.println("販売単価検索完了: 結果件数=" + (salesPrices != null ? salesPrices.size() : "null"));
            
            return priceExcelService.exportSalesPrices(salesPrices);
        } catch (Exception e) {
            System.out.println("販売単価出力サービスでエラー発生: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void downloadSalesPriceTemplate(HttpServletResponse response) throws IOException {
        // 実装
    }
}