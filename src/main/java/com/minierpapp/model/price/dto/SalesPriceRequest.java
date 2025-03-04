package com.minierpapp.model.price.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.minierpapp.model.common.Status;

@Data
public class SalesPriceRequest {
    private Long id;
    private Long itemId;
    private String itemCode;
    private Long customerId;
    private String customerCode;
    private BigDecimal basePrice;
    private String currencyCode = "JPY";
    private LocalDate validFromDate;
    private LocalDate validToDate;
    private Status status = Status.ACTIVE;
    private List<PriceScaleRequest> priceScales = new ArrayList<>();

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
} 