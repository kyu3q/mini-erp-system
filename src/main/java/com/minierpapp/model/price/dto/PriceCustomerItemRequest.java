package com.minierpapp.model.price.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class PriceCustomerItemRequest {
    private Long id;

    @NotBlank(message = "得意先コードは必須です")
    private String customerCode;

    @NotBlank(message = "商品コードは必須です")
    private String itemCode;

    @NotNull(message = "基本単価は必須です")
    @Positive(message = "基本単価は0より大きい値を入力してください")
    private BigDecimal basePrice;

    @NotBlank(message = "通貨コードは必須です")
    private String currencyCode;

    @Valid
    private List<PriceScaleRequest> priceScales = new ArrayList<>();
}