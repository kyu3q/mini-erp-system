package com.minierpapp.model.price.mapper;

import com.minierpapp.model.price.dto.PriceDto;
import com.minierpapp.model.price.dto.PriceResponse;
import com.minierpapp.model.price.entity.Price;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", uses = {PriceScaleMapper.class})
public interface PriceMapper {
    
    /**
     * エンティティをDTOに変換
     */
    @Mapping(target = "itemName", source = "item.itemName")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "priceScales", source = "priceScales")
    PriceDto toDto(Price entity);
    
    /**
     * エンティティのリストをDTOのリストに変換
     */
    List<PriceDto> toDtoList(List<Price> entities);
    
    /**
     * エンティティをレスポンスに変換
     */
    @Mapping(target = "itemName", source = "item.itemName")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "priceScales", source = "priceScales")
    PriceResponse entityToResponse(Price entity);
    
    /**
     * エンティティのリストをレスポンスのリストに変換
     */
    List<PriceResponse> toResponseList(List<Price> entities);
    
    /**
     * レスポンスのリストからエンティティのリストに変換
     */
    default List<Price> responsesToEntities(List<PriceResponse> responses) {
        // この実装は抽象クラスを直接インスタンス化できないため、
        // 実際の実装ではSalesPriceまたはPurchasePriceを使用する必要があります
        return null;
    }
} 