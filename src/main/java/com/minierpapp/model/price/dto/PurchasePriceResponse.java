package com.minierpapp.model.price.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.minierpapp.model.common.Status;

@Data
public class PurchasePriceResponse {
    private Long id;
    private Long itemId;
    private String itemCode;
    private String itemName;
    private Long supplierId;
    private String supplierCode;
    private String supplierName;
    private Long customerId;
    private String customerCode;
    private String customerName;
    private BigDecimal basePrice;
    private String currencyCode;
    private LocalDate validFromDate;
    private LocalDate validToDate;
    private Status status;
    private List<PriceScaleResponse> priceScales = new ArrayList<>();
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    
    // 追加の便利なフィールド
    // private boolean currentlyValid;
    // private boolean expiringSoon; // 30日以内に期限切れ
    // private String validityStatus; // "有効", "期限切れ", "期限切れ間近"
} 