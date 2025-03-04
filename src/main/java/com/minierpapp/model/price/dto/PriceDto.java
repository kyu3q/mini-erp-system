package com.minierpapp.model.price.dto;

import com.minierpapp.model.price.entity.PriceType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class PriceDto {
    private Long id;
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
    private String status;
    private List<PriceScaleDto> priceScales;
} 