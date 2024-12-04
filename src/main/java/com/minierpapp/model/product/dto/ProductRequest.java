package com.minierpapp.model.product.dto;

import com.minierpapp.model.common.Constants;
import com.minierpapp.model.common.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductRequest {

    @NotBlank
    @Size(max = Constants.CODE_LENGTH)
    private String productCode;

    @NotBlank
    @Size(max = Constants.NAME_LENGTH)
    private String productName;

    @Size(max = Constants.DESCRIPTION_LENGTH)
    private String description;

    @NotBlank
    @Size(max = 20)
    private String unit;

    @NotNull
    private Status status;

    private Integer minimumStock;

    private Integer maximumStock;

    private Integer reorderPoint;
}