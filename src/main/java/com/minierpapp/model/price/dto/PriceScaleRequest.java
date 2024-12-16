package com.minierpapp.model.price.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceScaleRequest {
    private Long id;

    @NotNull(message = "開始数量は必須です")
    @DecimalMin(value = "0.0", message = "開始数量は0以上である必要があります")
    @Digits(integer = 9, fraction = 3, message = "開始数量は整数部9桁、小数部3桁までです")
    private BigDecimal fromQuantity;

    @DecimalMin(value = "0.0", message = "終了数量は0以上である必要があります")
    @Digits(integer = 9, fraction = 3, message = "終了数量は整数部9桁、小数部3桁までです")
    private BigDecimal toQuantity;

    @NotNull(message = "スケール価格は必須です")
    @DecimalMin(value = "0.0", message = "スケール価格は0以上である必要があります")
    @Digits(integer = 10, fraction = 2, message = "スケール価格は整数部10桁、小数部2桁までです")
    private BigDecimal scalePrice;

    @NotBlank(message = "通貨コードは必須です")
    @Size(min = 3, max = 3, message = "通貨コードは3文字である必要があります")
    private String currencyCode = "JPY";
}