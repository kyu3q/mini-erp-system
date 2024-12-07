package com.minierpapp.service;

import com.minierpapp.model.product.Product;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {

    public byte[] exportProductsToExcel(List<Product> products) throws IOException {
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
            for (Product product : products) {
                Row row = sheet.createRow(rowNum++);
                
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(product.getProductCode());
                cell0.setCellStyle(dataStyle);

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(product.getProductName());
                cell1.setCellStyle(dataStyle);

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(product.getUnit());
                cell2.setCellStyle(dataStyle);

                Cell cell3 = row.createCell(3);
                cell3.setCellValue(product.getStatus().name().equals("ACTIVE") ? "有効" : "無効");
                cell3.setCellStyle(dataStyle);

                Cell cell4 = row.createCell(4);
                if (product.getMinimumStock() != null) {
                    cell4.setCellValue(product.getMinimumStock());
                } else {
                    cell4.setCellValue("");
                }
                cell4.setCellStyle(numberStyle);

                Cell cell5 = row.createCell(5);
                if (product.getMaximumStock() != null) {
                    cell5.setCellValue(product.getMaximumStock());
                } else {
                    cell5.setCellValue("");
                }
                cell5.setCellStyle(numberStyle);

                Cell cell6 = row.createCell(6);
                if (product.getReorderPoint() != null) {
                    cell6.setCellValue(product.getReorderPoint());
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
}