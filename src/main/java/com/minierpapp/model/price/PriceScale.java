package com.minierpapp.model.price;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

import com.minierpapp.model.base.BaseEntity;

@Entity
@Table(name = "price_scales")
@Data
@EqualsAndHashCode(callSuper = true)
public class PriceScale extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_condition_id", nullable = false)
    private PriceCondition priceCondition;

    @Column(name = "from_quantity", nullable = false)
    private BigDecimal fromQuantity;

    @Column(name = "to_quantity")
    private BigDecimal toQuantity;

    @Column(name = "scale_price", nullable = false)
    private BigDecimal scalePrice;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode = "JPY";
}