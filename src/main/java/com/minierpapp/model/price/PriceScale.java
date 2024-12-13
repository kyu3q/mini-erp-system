package com.minierpapp.model.price;

import com.minierpapp.model.common.BaseEntity;
import com.minierpapp.model.common.Status;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Entity
@Table(name = "price_scales")
@Data
@EqualsAndHashCode(callSuper = true)
public class PriceScale extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_id", nullable = false)
    private Price price;

    @Column(name = "from_quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal fromQuantity;

    @Column(name = "to_quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal toQuantity;

    @Column(name = "scale_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal scalePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;
}