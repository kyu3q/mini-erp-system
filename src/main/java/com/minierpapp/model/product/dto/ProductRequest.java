package com.minierpapp.model.product.dto;

import com.minierpapp.model.common.Constants;
import com.minierpapp.model.common.Status;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductRequest {

    private Long id;

    @NotBlank(message = "商品コードを入力してください")
    @Size(max = Constants.CODE_LENGTH, message = "商品コードは{max}文字以内で入力してください")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "商品コードは半角英数字のみ入力可能です")
    private String productCode;

    @NotBlank(message = "商品名を入力してください")
    @Size(max = Constants.NAME_LENGTH, message = "商品名は{max}文字以内で入力してください")
    private String productName;

    @Size(max = Constants.DESCRIPTION_LENGTH, message = "説明は{max}文字以内で入力してください")
    private String description;

    @NotBlank(message = "単位を入力してください")
    @Size(max = 20, message = "単位は{max}文字以内で入力してください")
    private String unit;

    @NotNull(message = "ステータスを選択してください")
    private Status status;

    @Min(value = 0, message = "最小在庫数は0以上の数値を入力してください")
    private Integer minimumStock;

    @Min(value = 0, message = "最大在庫数は0以上の数値を入力してください")
    private Integer maximumStock;

    @Min(value = 0, message = "発注点は0以上の数値を入力してください")
    private Integer reorderPoint;

    private Long version;
}