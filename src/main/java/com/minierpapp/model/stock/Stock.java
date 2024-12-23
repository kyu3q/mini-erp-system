package com.minierpapp.model.stock;

import com.minierpapp.model.common.BaseEntity;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.warehouse.Warehouse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stocks",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_stocks_warehouse_item_not_deleted",
                   columnNames = {"warehouse_id", "item_id", "deleted"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Stock extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "minimum_quantity")
    private Integer minimumQuantity;

    @Column(name = "maximum_quantity")
    private Integer maximumQuantity;

    @Column(name = "location")
    private String location;

    @Column(name = "notes")
    private String notes;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}