package com.minierpapp.model.price.dto;

import com.minierpapp.model.price.ConditionType;
import com.minierpapp.model.price.PriceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class PriceRequest {
    private Long id;

    @NotNull(message = "価格表種別は必須です")
    private PriceType priceType;

    @NotNull(message = "条件種別は必須です")
    private ConditionType conditionType;

    @NotNull(message = "適用開始日は必須です")
    private LocalDate validFromDate;

    @NotNull(message = "適用終了日は必須です")
    private LocalDate validToDate;

    @Valid
    private List<PriceItemRequest> priceItems = new ArrayList<>();

    @Valid
    private List<PriceSupplierItemRequest> priceSupplierItems = new ArrayList<>();

    @Valid
    private List<PriceCustomerItemRequest> priceCustomerItems = new ArrayList<>();

    @Valid
    private List<PriceSupplierCustomerItemRequest> priceSupplierCustomerItems = new ArrayList<>();
}