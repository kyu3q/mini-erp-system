package com.minierpapp.model.price;

import com.minierpapp.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "price_supplier_items")
@Data
@EqualsAndHashCode(callSuper = true)
public class PriceSupplierItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_id", nullable = false)
    private Price price;

    @Column(name = "supplier_code", nullable = false)
    private String supplierCode;

    @Column(name = "item_code", nullable = false)
    private String itemCode;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "price_supplier_item_id", nullable = false)
    private List<PriceScale> priceScales = new ArrayList<>();
}