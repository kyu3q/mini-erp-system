package com.minierpapp.model.price.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PriceScaleRequest {
    private Long id;

    @NotNull(message = "開始数量は必須です")
    @Positive(message = "開始数量は0より大きい値を入力してください")
    private BigDecimal fromQuantity;

    @NotNull(message = "終了数量は必須です")
    @Positive(message = "終了数量は0より大きい値を入力してください")
    private BigDecimal toQuantity;

    @NotNull(message = "単価は必須です")
    @Positive(message = "単価は0より大きい値を入力してください")
    private BigDecimal scalePrice;
}