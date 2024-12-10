package com.minierpapp.model.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockResponse {
    private Long id;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private Long itemId;
    private String itemCode;
    private String itemName;
    private Integer quantity;
    private Integer minimumQuantity;
    private Integer maximumQuantity;
    private String location;
    private String notes;
}