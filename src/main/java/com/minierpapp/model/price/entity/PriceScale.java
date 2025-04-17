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
    @JoinColumn(name = "price_id")
    private Price price;
    
    @Column(name = "price_id", insertable = false, updatable = false)
    private Long priceId;

    @Column(name = "from_quantity", nullable = false)
    private BigDecimal fromQuantity;
    
    @Column(name = "to_quantity")
    private BigDecimal toQuantity;
    
    @Column(name = "scale_price", nullable = false)
    private BigDecimal scalePrice;
} 