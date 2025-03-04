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
    
    @ManyToOne
    @JoinColumn(name = "price_id")
    private Price price;
    
    @Column(name = "from_quantity", nullable = false)
    private BigDecimal fromQuantity;
    
    @Column(name = "to_quantity")
    private BigDecimal toQuantity;
    
    @Column(name = "scale_price", nullable = false)
    private BigDecimal scalePrice;
} 