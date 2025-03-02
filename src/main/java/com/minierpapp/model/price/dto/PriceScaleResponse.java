package com.minierpapp.model.price.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PriceScaleResponse {
    private Long id;
    private Long priceConditionId;
    private BigDecimal fromQuantity;
    private BigDecimal toQuantity;
    private BigDecimal scalePrice;
}