package com.minierpapp.dto.excel;

import com.minierpapp.model.common.Status;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class PriceExcelRow {
    private Integer rowNum;
    private String itemCode;
    private String itemName;
    private String customerCode;
    private String customerName;
    private String supplierCode;
    private String supplierName;
    private BigDecimal basePrice;
    private String currencyCode;
    private LocalDate validFromDate;
    private LocalDate validToDate;
    private Status status;
    private List<PriceScaleExcelRow> scales = new ArrayList<>();

    @Data
    public static class PriceScaleExcelRow {
        private BigDecimal fromQuantity;
        private BigDecimal toQuantity;
        private BigDecimal scalePrice;
    }
}