package com.minierpapp.model.price.dto;

import com.minierpapp.model.common.Status;
import com.minierpapp.model.price.entity.PriceType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class PriceResponse {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private PriceType priceType;
    private Long itemId;
    private String itemCode;
    private String itemName;
    private Long customerId;
    private String customerCode;
    private String customerName;
    private Long supplierId;
    private String supplierCode;
    private String supplierName;
    private BigDecimal basePrice;
    private String currencyCode;
    private LocalDate validFromDate;
    private LocalDate validToDate;
    private Status status;
    private List<PriceScaleResponse> priceScales = new ArrayList<>();
}