package com.minierpapp.model.price.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class PriceSupplierItemResponse {
    private Long id;
    private String supplierCode;
    private String itemCode;
    private BigDecimal basePrice;
    private String currencyCode;
    private String status;
    private List<PriceScaleResponse> priceScales = new ArrayList<>();
}