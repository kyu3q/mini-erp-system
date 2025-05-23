package com.minierpapp.model.price.mapper;

import com.minierpapp.model.price.dto.PriceDto;
import com.minierpapp.model.price.dto.PriceResponse;
import com.minierpapp.model.price.entity.Price;

import org.mapstruct.*;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {PriceScaleMapper.class})
public interface PriceMapper {
    
    /**
     * エンティティをDTOに変換
     */
    @Mapping(target = "itemName", source = "item.itemName")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "priceScales", ignore = true)
    PriceDto toDto(Price entity);
    
    /**
     * エンティティをレスポンスに変換
     */
    @Mapping(target = "itemName", source = "item.itemName")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "priceScales", ignore = true)
    PriceResponse entityToResponse(Price entity);
    
    /**
     * レスポンスのリストからエンティティのリストに変換
     */
    default List<Price> responsesToEntities(List<PriceResponse> responses) {
        // この実装は抽象クラスを直接インスタンス化できないため、
        // 実際の実装ではSalesPriceまたはPurchasePriceを使用する必要があります
        return null;
    }

    /**
     * エンティティのリストをレスポンスのリストに変換
     */
    default List<PriceResponse> toResponseList(List<Price> entities) {
        return entities.stream()
            .map(this::entityToResponse)
            .collect(Collectors.toList());
    }

    /**
     * エンティティのリストをDTOのリストに変換
     */
    default List<PriceDto> toDtoList(List<Price> entities) {
        return entities.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
} 