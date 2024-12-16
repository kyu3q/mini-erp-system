package com.minierpapp.model.item.dto;

import com.minierpapp.model.common.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemDto {
    private Long id;

    @NotBlank(message = "品目コードは必須です")
    @Size(max = 20, message = "品目コードは20文字以内で入力してください")
    private String itemCode;

    @NotBlank(message = "品目名は必須です")
    @Size(max = 100, message = "品目名は100文字以内で入力してください")
    private String itemName;

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

    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private boolean deleted;
}