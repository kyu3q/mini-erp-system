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
@Table(name = "price_conditions")
@Getter
@Setter
public class PriceCondition extends BaseEntity {
    
    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false)
    private PriceType priceType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
    
    @Column(name = "item_code", nullable = false)
    private String itemCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    @Column(name = "customer_code")
    private String customerCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
    
    @Column(name = "supplier_code")
    private String supplierCode;
    
    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;
    
    @Column(name = "currency_code", nullable = false)
    private String currencyCode = "JPY";
    
    @Column(name = "valid_from_date", nullable = false)
    private LocalDate validFromDate;
    
    @Column(name = "valid_to_date", nullable = false)
    private LocalDate validToDate;
    
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;
    
    @OneToMany(mappedBy = "priceCondition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceScale> priceScales = new ArrayList<>();
    
    // ヘルパーメソッド
    public void addPriceScale(PriceScale priceScale) {
        priceScales.add(priceScale);
        priceScale.setPriceCondition(this);
    }
    
    public void removePriceScale(PriceScale priceScale) {
        priceScales.remove(priceScale);
        priceScale.setPriceCondition(null);
    }
    
    // 価格計算メソッド
    public BigDecimal calculatePrice(BigDecimal quantity) {
        if (priceScales.isEmpty()) {
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
    
    // 販売価格かどうかを判定
    public boolean isSalesPrice() {
        return PriceType.SALES.equals(priceType);
    }
    
    // 仕入価格かどうかを判定
    public boolean isPurchasePrice() {
        return PriceType.PURCHASE.equals(priceType);
    }
    
    // 特定顧客向けの価格かどうかを判定
    public boolean isCustomerSpecific() {
        return customer != null;
    }
    
    // 特定仕入先向けの価格かどうかを判定
    public boolean isSupplierSpecific() {
        return supplier != null;
    }
    
    // 価格が現在有効かどうかを判定
    public boolean isCurrentlyValid() {
        LocalDate today = LocalDate.now();
        return !isDeleted() && 
               "ACTIVE".equals(status.name()) && 
               !validFromDate.isAfter(today) && 
               !validToDate.isBefore(today);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setItemId(Long itemId) {
        if (this.item == null) {
            Item item = new Item();
            item.setId(itemId);
            this.item = item;
        }
    }

    public void setSupplierId(Long supplierId) {
        if (this.supplier == null) {
            Supplier supplier = new Supplier();
            supplier.setId(supplierId);
            this.supplier = supplier;
        }
    }
}