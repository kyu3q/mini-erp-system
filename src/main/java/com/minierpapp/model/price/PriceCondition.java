package com.minierpapp.model.price;

import com.minierpapp.model.common.BaseEntity;
import com.minierpapp.model.common.Status;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.supplier.Supplier;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "price_conditions")
@Data
@EqualsAndHashCode(callSuper = true)
public class PriceCondition extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false)
    private PriceType priceType;

    @Column(name = "item_code")
    private String itemCode;

    @Column(name = "customer_code")
    private String customerCode;

    @Column(name = "supplier_code")
    private String supplierCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode = "JPY";

    @Column(name = "valid_from_date", nullable = false)
    private LocalDate validFromDate;

    @Column(name = "valid_to_date", nullable = false)
    private LocalDate validToDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;

    @OneToMany(mappedBy = "priceCondition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceScale> priceScales = new ArrayList<>();

    public boolean isExpired() {
        return LocalDate.now().isAfter(validToDate);
    }

    public boolean isExpiringSoon() {
        LocalDate now = LocalDate.now();
        return !isExpired() && now.plusDays(30).isAfter(validToDate);
    }
}