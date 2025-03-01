package com.minierpapp.model.price.entity;

import com.minierpapp.model.base.BaseEntity;
import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.common.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_prices")
@Getter
@Setter
public class SalesPrice extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;
    
    @Column(name = "item_code")
    private String itemCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    @Column(name = "customer_code")
    private String customerCode;
    
    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;
    
    @Column(name = "currency_code", nullable = false)
    private String currencyCode;
    
    @Column(name = "valid_from_date", nullable = false)
    private LocalDate validFromDate;
    
    @Column(name = "valid_to_date", nullable = false)
    private LocalDate validToDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;
    
    @OneToMany(mappedBy = "salesPrice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceScale> priceScales = new ArrayList<>();
    
    @Column(name = "remarks")
    private String remarks;
} 