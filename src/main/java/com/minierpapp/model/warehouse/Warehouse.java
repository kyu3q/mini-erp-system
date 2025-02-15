package com.minierpapp.model.warehouse;

import com.minierpapp.model.base.BaseEntity;
import com.minierpapp.model.common.Constants;
import com.minierpapp.model.common.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "warehouses",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_warehouses_warehouse_code_not_deleted",
                        columnNames = {"warehouse_code", "deleted"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse extends BaseEntity {

    @Column(name = "warehouse_code", nullable = false)
    private String warehouseCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "capacity")
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;

    @Size(max = Constants.DESCRIPTION_LENGTH)
    @Column(name = "description", length = Constants.DESCRIPTION_LENGTH)
    private String description;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}