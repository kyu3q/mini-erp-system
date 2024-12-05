package com.minierpapp.model.product;

import com.minierpapp.model.common.BaseEntity;
import com.minierpapp.model.common.Constants;
import com.minierpapp.model.common.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product extends BaseEntity {

    @NotBlank
    @Size(max = Constants.CODE_LENGTH)
    @Column(name = "product_code", length = Constants.CODE_LENGTH, nullable = false, unique = true)
    private String productCode;

    @NotBlank
    @Size(max = Constants.NAME_LENGTH)
    @Column(name = "product_name", length = Constants.NAME_LENGTH, nullable = false)
    private String productName;

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

    @Version
    @Column(name = "version")
    private Long version;
}