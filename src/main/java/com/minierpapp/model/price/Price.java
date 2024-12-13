package com.minierpapp.model.price;

import com.minierpapp.model.common.BaseEntity;
import com.minierpapp.model.common.Status;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "prices")
public class Price extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false)
    private PriceType priceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false)
    private ConditionType conditionType;

    @Column(name = "valid_from_date", nullable = false)
    private LocalDate validFromDate;

    @Column(name = "valid_to_date", nullable = false)
    private LocalDate validToDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;

    @OneToMany(mappedBy = "price", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceItem> priceItems = new ArrayList<>();

    @OneToMany(mappedBy = "price", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceSupplierItem> priceSupplierItems = new ArrayList<>();

    @OneToMany(mappedBy = "price", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceCustomerItem> priceCustomerItems = new ArrayList<>();

    @OneToMany(mappedBy = "price", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceSupplierCustomerItem> priceSupplierCustomerItems = new ArrayList<>();

    public boolean isExpired() {
        return LocalDate.now().isAfter(validToDate);
    }

    public boolean isExpiringSoon() {
        LocalDate now = LocalDate.now();
        return !isExpired() && now.plusDays(30).isAfter(validToDate);
    }
}