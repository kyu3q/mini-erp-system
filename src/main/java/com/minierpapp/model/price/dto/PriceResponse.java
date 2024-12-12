package com.minierpapp.model.price.dto;

import com.minierpapp.model.price.ConditionType;
import com.minierpapp.model.price.PriceType;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class PriceResponse {
    private Long id;
    private PriceType priceType;
    private ConditionType conditionType;
    private LocalDate validFromDate;
    private LocalDate validToDate;
    private String status;
    private List<PriceItemResponse> priceItems = new ArrayList<>();
    private List<PriceSupplierItemResponse> priceSupplierItems = new ArrayList<>();
    private List<PriceCustomerItemResponse> priceCustomerItems = new ArrayList<>();
    private List<PriceSupplierCustomerItemResponse> priceSupplierCustomerItems = new ArrayList<>();
}