package com.minierpapp.model.price.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class PriceScaleResponse {
    private Long id;
    private Long priceId;
    private BigDecimal fromQuantity;
    private BigDecimal toQuantity;
    private BigDecimal scalePrice;
}