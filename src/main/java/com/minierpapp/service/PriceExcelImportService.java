package com.minierpapp.service;

import com.minierpapp.model.price.*;
import com.minierpapp.model.price.ConditionType;
import com.minierpapp.model.price.dto.PriceRequest;
import com.minierpapp.model.price.dto.PriceItemRequest;
import com.minierpapp.model.price.dto.PriceCustomerItemRequest;
import com.minierpapp.model.price.dto.PriceSupplierCustomerItemRequest;
import com.minierpapp.model.price.dto.PriceScaleRequest;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceExcelImportService {
    private final PriceService priceService;

    public void importPrices(MultipartFile file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            PriceRequest priceRequest = null;
            List<PriceItemRequest> priceItems = new ArrayList<>();
            List<PriceCustomerItemRequest> priceCustomerItems = new ArrayList<>();
            List<PriceSupplierCustomerItemRequest> priceSupplierCustomerItems = new ArrayList<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // ヘッダー行をスキップ

                if (priceRequest == null) {
                    priceRequest = createPriceRequest(row);
                } else {
                    String recordType = getCellStringValue(row.getCell(0));
                    switch (recordType) {
                        case "ITEM" -> priceItems.add(createPriceItemRequest(row));
                        case "CUSTOMER_ITEM" -> priceCustomerItems.add(createPriceCustomerItemRequest(row));
                        case "SUPPLIER_CUSTOMER_ITEM" -> priceSupplierCustomerItems.add(createPriceSupplierCustomerItemRequest(row));
                    }
                }
            }

            if (priceRequest != null) {
                priceRequest.setPriceItems(priceItems);
                priceRequest.setPriceCustomerItems(priceCustomerItems);
                priceRequest.setPriceSupplierCustomerItems(priceSupplierCustomerItems);
                priceService.createPrice(priceRequest);
            }
        }
    }

    private PriceRequest createPriceRequest(Row row) {
        PriceRequest request = new PriceRequest();
        request.setPriceType(PriceType.valueOf(getCellStringValue(row.getCell(1))));
        request.setConditionType(ConditionType.valueOf(getCellStringValue(row.getCell(2))));
        request.setValidFromDate(row.getCell(3).getLocalDateTimeCellValue().toLocalDate());
        request.setValidToDate(row.getCell(4).getLocalDateTimeCellValue().toLocalDate());
        return request;
    }

    private PriceItemRequest createPriceItemRequest(Row row) {
        PriceItemRequest request = new PriceItemRequest();
        request.setItemCode(getCellStringValue(row.getCell(1)));
        request.setBasePrice(BigDecimal.valueOf(row.getCell(2).getNumericCellValue()));
        request.setCurrencyCode(getCellStringValue(row.getCell(3)));
        request.setPriceScales(createPriceScales(row));
        return request;
    }

    private PriceCustomerItemRequest createPriceCustomerItemRequest(Row row) {
        PriceCustomerItemRequest request = new PriceCustomerItemRequest();
        request.setCustomerCode(getCellStringValue(row.getCell(1)));
        request.setItemCode(getCellStringValue(row.getCell(2)));
        request.setBasePrice(BigDecimal.valueOf(row.getCell(3).getNumericCellValue()));
        request.setCurrencyCode(getCellStringValue(row.getCell(4)));
        request.setPriceScales(createPriceScales(row));
        return request;
    }

    private PriceSupplierCustomerItemRequest createPriceSupplierCustomerItemRequest(Row row) {
        PriceSupplierCustomerItemRequest request = new PriceSupplierCustomerItemRequest();
        request.setSupplierCode(getCellStringValue(row.getCell(1)));
        request.setCustomerCode(getCellStringValue(row.getCell(2)));
        request.setItemCode(getCellStringValue(row.getCell(3)));
        request.setBasePrice(BigDecimal.valueOf(row.getCell(4).getNumericCellValue()));
        request.setCurrencyCode(getCellStringValue(row.getCell(5)));
        request.setPriceScales(createPriceScales(row));
        return request;
    }

    private List<PriceScaleRequest> createPriceScales(Row row) {
        List<PriceScaleRequest> scales = new ArrayList<>();
        int startCol = row.getLastCellNum();
        for (int i = startCol; i < row.getLastCellNum(); i += 3) {
            Cell fromCell = row.getCell(i);
            Cell toCell = row.getCell(i + 1);
            Cell priceCell = row.getCell(i + 2);
            if (fromCell == null || toCell == null || priceCell == null) break;

            PriceScaleRequest scale = new PriceScaleRequest();
            scale.setFromQuantity(BigDecimal.valueOf(fromCell.getNumericCellValue()));
            scale.setToQuantity(BigDecimal.valueOf(toCell.getNumericCellValue()));
            scale.setScalePrice(BigDecimal.valueOf(priceCell.getNumericCellValue()));
            scales.add(scale);
        }
        return scales;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING -> {
                return cell.getStringCellValue();
            }
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            }
            default -> {
                return "";
            }
        }
    }
}