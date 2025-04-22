package com.minierpapp.model.price.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SalesPriceSearchCriteria {
    private String itemCode;
    private String itemName;
    private String customerCode;
    private String customerName;
    private LocalDate validDate;
    private String status;
    
    @Override
    public String toString() {
        return "SalesPriceSearchCriteria{" +
                "itemCode='" + itemCode + '\'' +
                ", customerCode='" + customerCode + '\'' +
                ", validDate=" + validDate +
                ", status='" + status + '\'' +
                '}';
    }

    public SalesPriceSearchCriteria(String itemCode, String itemName, String customerCode, String customerName, LocalDate validDate, String status) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.customerCode = customerCode;
        this.customerName = customerName;
        this.validDate = validDate;
        this.status = status;
    }

    public LocalDate getValidDate() {
        return validDate != null ? validDate : LocalDate.now();
    }
} 