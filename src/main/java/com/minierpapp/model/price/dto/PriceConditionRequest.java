package com.minierpapp.model.price.dto;

import com.minierpapp.model.common.Status;
import com.minierpapp.model.price.PriceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class PriceConditionRequest {
    private Long id;

    @NotNull(message = "価格タイプは必須です")
    private PriceType priceType;

    @NotNull(message = "品目は必須です")
    private Long itemId;
    private String itemCode;

    private Long customerId;
    private String customerCode;

    private Long supplierId;
    private String supplierCode;

    @NotNull(message = "基本価格は必須です")
    @DecimalMin(value = "0.0", message = "基本価格は0以上である必要があります")
    @Digits(integer = 10, fraction = 2, message = "基本価格は整数部10桁、小数部2桁までです")
    private BigDecimal basePrice;

    @NotBlank(message = "通貨コードは必須です")
    @Size(min = 3, max = 3, message = "通貨コードは3文字である必要があります")
    private String currencyCode = "JPY";

    @NotNull(message = "有効開始日は必須です")
    private LocalDate validFromDate;

    @NotNull(message = "有効終了日は必須です")
    private LocalDate validToDate;

    @NotNull(message = "ステータスは必須です")
    private Status status = Status.ACTIVE;

    @Valid
    private List<PriceScaleRequest> priceScales = new ArrayList<>();
}