package com.minierpapp.model.price.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PriceScaleResponse {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private BigDecimal fromQuantity;
    private BigDecimal toQuantity;
    private BigDecimal scalePrice;
    private String currencyCode;
}