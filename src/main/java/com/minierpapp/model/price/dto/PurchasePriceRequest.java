package com.minierpapp.model.price.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.minierpapp.model.common.Status;

@Data
public class PurchasePriceRequest {
    private Long id;
    private Long itemId;
    private String itemCode;
    private Long supplierId;
    private String supplierCode;
    private Long customerId;
    private String customerCode;
    private BigDecimal basePrice;
    private String currencyCode = "JPY";
    private LocalDate validFromDate;
    private LocalDate validToDate;
    private Status status = Status.ACTIVE;
    private List<PriceScaleRequest> priceScales = new ArrayList<>();
} 