package com.minierpapp.model.price.entity;

import com.minierpapp.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "price_scales")
@Getter
@Setter
public class PriceScale extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_condition_id")
    private PriceCondition priceCondition;
    
    @Column(name = "from_quantity", nullable = false)
    private BigDecimal fromQuantity;
    
    @Column(name = "to_quantity")
    private BigDecimal toQuantity;
    
    @Column(name = "scale_price", nullable = false)
    private BigDecimal scalePrice;
    
    @Column(name = "currency_code", length = 3)
    private String currencyCode;
    
    // 割引率を計算（基本価格との差をパーセンテージで）
    public BigDecimal calculateDiscountPercentage() {
        if (priceCondition == null || priceCondition.getBasePrice().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal basePrice = priceCondition.getBasePrice();
        BigDecimal difference = basePrice.subtract(scalePrice);
        return difference.multiply(new BigDecimal("100")).divide(basePrice, 2, BigDecimal.ROUND_HALF_UP);
    }
    
    // 割引額を計算（基本価格との差額）
    public BigDecimal calculateDiscountAmount() {
        if (priceCondition == null) {
            return BigDecimal.ZERO;
        }
        
        return priceCondition.getBasePrice().subtract(scalePrice);
    }
} 