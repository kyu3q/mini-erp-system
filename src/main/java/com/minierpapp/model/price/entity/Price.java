package com.minierpapp.model.price.entity;

import com.minierpapp.model.base.BaseEntity;
import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.supplier.Supplier;
import com.minierpapp.model.common.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prices")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "price_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public class Price extends BaseEntity {
    
    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", insertable = false, updatable = false)
    private PriceType priceType;
    
    @Column(name = "item_id")
    private Long itemId;
    
    @Column(name = "item_code")
    private String itemCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private Item item;
    
    @Column(name = "customer_id")
    private Long customerId;
    
    @Column(name = "customer_code")
    private String customerCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;
    
    @Column(name = "supplier_id")
    private Long supplierId;
    
    @Column(name = "supplier_code")
    private String supplierCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", insertable = false, updatable = false)
    private Supplier supplier;
    
    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;
    
    @Column(name = "currency_code", length = 3)
    private String currencyCode;
    
    @Column(name = "valid_from_date", nullable = false)
    private LocalDate validFromDate;
    
    @Column(name = "valid_to_date", nullable = false)
    private LocalDate validToDate;
    
    @Column(name = "status", length = 20)
    private String status;
    
    @OneToMany(mappedBy = "price", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceScale> priceScales = new ArrayList<>();

    public boolean isCurrentlyValid() {
        LocalDate today = LocalDate.now();
        return validFromDate.compareTo(today) <= 0 && validToDate.compareTo(today) >= 0;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public void setStatus(Status status) {
        this.status = status.name();
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
        if (this.item == null) {
            Item item = new Item();
            item.setId(itemId);
            this.item = item;
        }
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
        if (this.supplier == null) {
            Supplier supplier = new Supplier();
            supplier.setId(supplierId);
            this.supplier = supplier;
        }
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
        if (this.customer == null) {
            Customer customer = new Customer();
            customer.setId(customerId);
            this.customer = customer;
        }
    }

    /**
     * 仕入価格かどうかを判定
     */
    public boolean isPurchasePrice() {
        return PriceType.PURCHASE.equals(priceType);
    }

    /**
     * 販売価格かどうかを判定
     */
    public boolean isSalesPrice() {
        return PriceType.SALES.equals(priceType);
    }

    /**
     * 数量に基づいて価格を計算
     */
    public BigDecimal calculatePrice(BigDecimal quantity) {
        if (priceScales == null || priceScales.isEmpty()) {
            return basePrice;
        }
        
        // 数量に適用可能なスケールを検索
        return priceScales.stream()
            .filter(scale -> scale.getFromQuantity().compareTo(quantity) <= 0 && 
                   (scale.getToQuantity() == null || scale.getToQuantity().compareTo(quantity) >= 0))
            .map(PriceScale::getScalePrice)
            .findFirst()
            .orElse(basePrice);
    }

    /**
     * 価格スケールを追加
     */
    public void addPriceScale(PriceScale priceScale) {
        priceScales.add(priceScale);
        priceScale.setPrice(this);
    }

    /**
     * 価格スケールを削除
     */
    public void removePriceScale(PriceScale priceScale) {
        priceScales.remove(priceScale);
        priceScale.setPrice(null);
    }
} 