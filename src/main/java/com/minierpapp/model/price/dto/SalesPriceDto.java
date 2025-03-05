package com.minierpapp.model.price.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.minierpapp.model.common.Status;

@Data
public class SalesPriceDto {
    private Long id;
    private Long itemId;
    private String itemCode;
    private String itemName;
    private Long customerId;
    private String customerCode;
    private String customerName;
    private BigDecimal basePrice;
    private String currencyCode;
    private LocalDate validFromDate;
    private LocalDate validToDate;
    private Status status = Status.ACTIVE;
    private List<PriceScaleDto> priceScales = new ArrayList<>();
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private boolean deleted;
} 