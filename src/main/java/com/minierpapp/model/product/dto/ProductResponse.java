package com.minierpapp.model.product.dto;

import com.minierpapp.model.common.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductResponse {
    private Long id;
    private String itemCode;
    private String itemName;
    private String description;
    private String unit;
    private Status status;
    private Integer minimumStock;
    private Integer maximumStock;
    private Integer reorderPoint;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}