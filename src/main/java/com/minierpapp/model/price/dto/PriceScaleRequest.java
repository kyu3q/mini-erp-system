package com.minierpapp.model.price.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PriceScaleRequest {
    private Long id;
    private BigDecimal quantity;
    private BigDecimal price;
    
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}