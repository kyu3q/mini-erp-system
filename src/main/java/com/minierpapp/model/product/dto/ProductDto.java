package com.minierpapp.model.product.dto;

import com.minierpapp.model.common.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductDto {
    private Long id;

    @NotBlank(message = "商品コードは必須です")
    @Size(max = 20, message = "商品コードは20文字以内で入力してください")
    private String productCode;

    @NotBlank(message = "商品名は必須です")
    @Size(max = 100, message = "商品名は100文字以内で入力してください")
    private String productName;

    @Size(max = 500, message = "説明は500文字以内で入力してください")
    private String description;

    @NotBlank(message = "単位は必須です")
    @Size(max = 20, message = "単位は20文字以内で入力してください")
    private String unit;

    @NotNull(message = "ステータスは必須です")
    private Status status;

    private Integer minimumStock;
    private Integer maximumStock;
    private Integer reorderPoint;
    private Long version;
}