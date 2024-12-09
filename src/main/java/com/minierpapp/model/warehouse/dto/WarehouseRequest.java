package com.minierpapp.model.warehouse.dto;

import com.minierpapp.model.common.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseRequest {
    
    private Long id;
    
    @NotBlank(message = "倉庫コードを入力してください")
    @Size(max = 50, message = "倉庫コードは50文字以内で入力してください")
    private String warehouseCode;

    @NotBlank(message = "名称を入力してください")
    @Size(max = 100, message = "名称は100文字以内で入力してください")
    private String name;

    @Size(max = 200, message = "住所は200文字以内で入力してください")
    private String address;

    private Integer capacity;

    @NotNull(message = "ステータスを選択してください")
    private Status status;

    @Size(max = 500, message = "説明は500文字以内で入力してください")
    private String description;
}