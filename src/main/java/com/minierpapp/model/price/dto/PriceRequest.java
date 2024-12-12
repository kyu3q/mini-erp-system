package com.minierpapp.model.price.dto;

import com.minierpapp.model.price.ConditionType;
import com.minierpapp.model.price.PriceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class PriceRequest {
    private Long id;

    @NotBlank(message = "価格表番号は必須です")
    private String priceNumber;

    @NotBlank(message = "価格表名は必須です")
    private String priceName;

    @NotNull(message = "価格表種別は必須です")
    private PriceType priceType;

    @NotNull(message = "条件種別は必須です")
    private ConditionType conditionType;

    @NotNull(message = "適用開始日は必須です")
    private LocalDate startDate;

    @NotNull(message = "適用終了日は必須です")
    private LocalDate endDate;

    @Valid
    private List<PriceItemRequest> priceItems = new ArrayList<>();

    @Valid
    private List<PriceCustomerItemRequest> priceCustomerItems = new ArrayList<>();

    @Valid
    private List<PriceSupplierCustomerItemRequest> priceSupplierCustomerItems = new ArrayList<>();
}