package com.minierpapp.model.price.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PriceScaleDto {
    private Long id;
    private Long priceConditionId;
    private BigDecimal fromQuantity;
    private BigDecimal toQuantity;
    private BigDecimal scalePrice;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    
    // // 追加の便利なフィールド
    // private BigDecimal discountPercentage;
    // private BigDecimal discountAmount;
    // private String quantityRange; // "10-100" のような表示用文字列
} 