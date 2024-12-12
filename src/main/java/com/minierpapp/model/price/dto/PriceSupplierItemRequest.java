package com.minierpapp.model.price.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PriceSupplierItemRequest {
    private String supplierCode;
    private String itemCode;
    private BigDecimal basePrice;
    private String currencyCode;
    private List<PriceScaleRequest> priceScales;
}