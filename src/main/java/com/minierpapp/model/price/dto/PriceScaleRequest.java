package com.minierpapp.model.price.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PriceScaleRequest {
    private Long id;
    private Long priceId;
    private BigDecimal fromQuantity;
    private BigDecimal toQuantity;
    private BigDecimal scalePrice;
}