package com.minierpapp.model.warehouse.dto;

import com.minierpapp.model.common.Status;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class WarehouseDto {
    private Long id;
    private String warehouseCode;
    private String name;
    private String address;
    private Integer capacity;
    private Status status;
    private String description;
}
