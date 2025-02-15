package com.minierpapp.model.item;

import com.minierpapp.model.base.BaseEntity;
import com.minierpapp.model.common.Constants;
import com.minierpapp.model.common.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_items_item_code_not_deleted",
                        columnNames = {"item_code", "deleted"})
        })
@Getter
@Setter
public class Item extends BaseEntity {

    @NotBlank
    @Size(max = Constants.CODE_LENGTH)
    @Column(name = "item_code", length = Constants.CODE_LENGTH, nullable = false)
    private String itemCode;

    @NotBlank
    @Size(max = Constants.NAME_LENGTH)
    @Column(name = "item_name", length = Constants.NAME_LENGTH, nullable = false)
    private String itemName;

    @Size(max = Constants.DESCRIPTION_LENGTH)
    @Column(name = "description", length = Constants.DESCRIPTION_LENGTH)
    private String description;

    @NotBlank
    @Size(max = 20)
    @Column(name = "unit", length = 20, nullable = false)
    private String unit;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;

    @Column(name = "minimum_stock")
    private Integer minimumStock;

    @Column(name = "maximum_stock")
    private Integer maximumStock;

    @Column(name = "reorder_point")
    private Integer reorderPoint;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}