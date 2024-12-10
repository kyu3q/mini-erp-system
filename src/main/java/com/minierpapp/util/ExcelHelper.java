package com.minierpapp.util;

import com.minierpapp.model.order.OrderStatus;
import com.minierpapp.model.order.dto.OrderDto;
import com.minierpapp.model.order.dto.OrderRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelHelper {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public void exportOrdersToExcel(HttpServletResponse response, List<OrderDto> orders) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=orders.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Orders");

            // ヘッダー行の作成
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "受注番号", "受注日", "得意先コード", "得意先名", "配送予定日", "配送先住所",
                "配送先郵便番号", "配送先電話番号", "配送先担当者", "ステータス", "備考",
                "合計金額", "消費税額"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // データ行の作成
            int rowNum = 1;
            for (OrderDto order : orders) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(order.getOrderNumber());
                row.createCell(1).setCellValue(order.getOrderDate().format(DATE_FORMATTER));
                row.createCell(2).setCellValue(order.getCustomerId().toString());
                row.createCell(3).setCellValue(order.getCustomerName());
                if (order.getDeliveryDate() != null) {
                    row.createCell(4).setCellValue(order.getDeliveryDate().format(DATE_FORMATTER));
                }
                row.createCell(5).setCellValue(order.getShippingAddress());
                row.createCell(6).setCellValue(order.getShippingPostalCode());
                row.createCell(7).setCellValue(order.getShippingPhone());
                row.createCell(8).setCellValue(order.getShippingContactPerson());
                row.createCell(9).setCellValue(order.getStatus().name());
                row.createCell(10).setCellValue(order.getNotes());
                row.createCell(11).setCellValue(order.getTotalAmount().doubleValue());
                row.createCell(12).setCellValue(order.getTaxAmount().doubleValue());
            }

            // 明細シートの作成
            Sheet detailSheet = workbook.createSheet("OrderDetails");
            Row detailHeaderRow = detailSheet.createRow(0);
            String[] detailHeaders = {
                "受注番号", "行番号", "品目コード", "品目名", "数量", "単価",
                "金額", "倉庫コード", "倉庫名", "配送予定日", "備考"
            };
            for (int i = 0; i < detailHeaders.length; i++) {
                Cell cell = detailHeaderRow.createCell(i);
                cell.setCellValue(detailHeaders[i]);
            }

            rowNum = 1;
            for (OrderDto order : orders) {
                for (OrderDto.OrderDetailDto detail : order.getOrderDetails()) {
                    Row row = detailSheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(order.getOrderNumber());
                    row.createCell(1).setCellValue(detail.getLineNumber());
                    row.createCell(2).setCellValue(detail.getItemCode());
                    row.createCell(3).setCellValue(detail.getItemName());
                    row.createCell(4).setCellValue(detail.getQuantity());
                    row.createCell(5).setCellValue(detail.getUnitPrice().doubleValue());
                    row.createCell(6).setCellValue(detail.getAmount().doubleValue());
                    if (detail.getWarehouseId() != null) {
                        row.createCell(7).setCellValue(detail.getWarehouseId().toString());
                        row.createCell(8).setCellValue(detail.getWarehouseName());
                    }
                    if (detail.getDeliveryDate() != null) {
                        row.createCell(9).setCellValue(detail.getDeliveryDate().format(DATE_FORMATTER));
                    }
                    row.createCell(10).setCellValue(detail.getNotes());
                }
            }

            // 列幅の自動調整
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            for (int i = 0; i < detailHeaders.length; i++) {
                detailSheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

    public List<OrderRequest> importOrdersFromExcel(MultipartFile file) throws IOException {
        List<OrderRequest> orders = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheet("Orders");
            if (sheet == null) {
                throw new IllegalArgumentException("Ordersシートが見つかりません。");
            }

            Sheet detailSheet = workbook.getSheet("OrderDetails");
            if (detailSheet == null) {
                throw new IllegalArgumentException("OrderDetailsシートが見つかりません。");
            }

            // ヘッダー行をスキップ
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                OrderRequest order = new OrderRequest();
                order.setOrderNumber(getStringValue(row.getCell(0)));
                order.setOrderDate(getDateValue(row.getCell(1)));
                order.setCustomerId(getLongValue(row.getCell(2)));
                order.setDeliveryDate(getDateValue(row.getCell(4)));
                order.setShippingAddress(getStringValue(row.getCell(5)));
                order.setShippingPostalCode(getStringValue(row.getCell(6)));
                order.setShippingPhone(getStringValue(row.getCell(7)));
                order.setShippingContactPerson(getStringValue(row.getCell(8)));
                order.setStatus(OrderStatus.valueOf(getStringValue(row.getCell(9))));
                order.setNotes(getStringValue(row.getCell(10)));
                order.setTotalAmount(getBigDecimalValue(row.getCell(11)));
                order.setTaxAmount(getBigDecimalValue(row.getCell(12)));

                // 明細データの取得
                List<OrderRequest.OrderDetailRequest> details = new ArrayList<>();
                for (int j = 1; j <= detailSheet.getLastRowNum(); j++) {
                    Row detailRow = detailSheet.getRow(j);
                    if (detailRow == null) continue;

                    String orderNumber = getStringValue(detailRow.getCell(0));
                    if (!order.getOrderNumber().equals(orderNumber)) continue;

                    OrderRequest.OrderDetailRequest detail = new OrderRequest.OrderDetailRequest();
                    detail.setLineNumber(getIntegerValue(detailRow.getCell(1)));
                    detail.setItemId(getLongValue(detailRow.getCell(2)));
                    detail.setQuantity(getIntegerValue(detailRow.getCell(4)));
                    detail.setUnitPrice(getBigDecimalValue(detailRow.getCell(5)));
                    detail.setAmount(getBigDecimalValue(detailRow.getCell(6)));
                    detail.setWarehouseId(getLongValue(detailRow.getCell(7)));
                    detail.setDeliveryDate(getDateValue(detailRow.getCell(9)));
                    detail.setNotes(getStringValue(detailRow.getCell(10)));
                    details.add(detail);
                }
                order.setOrderDetails(details);
                orders.add(order);
            }
        }
        return orders;
    }

    private String getStringValue(Cell cell) {
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue();
    }

    private LocalDate getDateValue(Cell cell) {
        if (cell == null) return null;
        try {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        } catch (Exception e) {
            String dateStr = getStringValue(cell);
            return dateStr != null ? LocalDate.parse(dateStr, DATE_FORMATTER) : null;
        }
    }

    private Long getLongValue(Cell cell) {
        if (cell == null) return null;
        String value = getStringValue(cell);
        return value != null ? Long.parseLong(value) : null;
    }

    private Integer getIntegerValue(Cell cell) {
        if (cell == null) return null;
        cell.setCellType(CellType.NUMERIC);
        return (int) cell.getNumericCellValue();
    }

    private BigDecimal getBigDecimalValue(Cell cell) {
        if (cell == null) return null;
        cell.setCellType(CellType.NUMERIC);
        return BigDecimal.valueOf(cell.getNumericCellValue());
    }
}