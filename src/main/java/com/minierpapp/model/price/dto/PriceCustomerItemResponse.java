package com.minierpapp.model.price.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class PriceCustomerItemResponse {
    private Long id;
    private String customerCode;
    private String itemCode;
    private BigDecimal basePrice;
    private String currencyCode;
    private List<PriceScaleResponse> priceScales = new ArrayList<>();
}