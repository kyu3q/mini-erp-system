package com.minierpapp.model.price;

import com.minierpapp.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prices")
@Data
@EqualsAndHashCode(callSuper = true)
public class Price extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(nullable = false)
    private String status;

    @OneToMany(mappedBy = "price", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceItem> priceItems = new ArrayList<>();

    @OneToMany(mappedBy = "price", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceSupplierItem> priceSupplierItems = new ArrayList<>();

    @OneToMany(mappedBy = "price", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceCustomerItem> priceCustomerItems = new ArrayList<>();

    @OneToMany(mappedBy = "price", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceSupplierCustomerItem> priceSupplierCustomerItems = new ArrayList<>();

    @OneToMany(mappedBy = "price", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceScale> priceScales = new ArrayList<>();
}