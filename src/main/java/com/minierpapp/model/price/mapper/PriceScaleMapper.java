package com.minierpapp.model.price.mapper;

import com.minierpapp.model.base.BaseMapper;
import com.minierpapp.model.price.dto.PriceScaleDto;
import com.minierpapp.model.price.dto.PriceScaleRequest;
import com.minierpapp.model.price.dto.PriceScaleResponse;
import com.minierpapp.model.price.entity.PriceScale;
import org.mapstruct.*;
import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PriceScaleMapper extends BaseMapper<PriceScale, PriceScaleDto, PriceScaleRequest, PriceScaleResponse> {
    
    @Override
    @Mapping(target = "priceConditionId", source = "priceCondition.id")
    // @Mapping(target = "discountPercentage", expression = "java(calculateDiscountPercentage(entity))")
    // @Mapping(target = "discountAmount", expression = "java(calculateDiscountAmount(entity))")
    // @Mapping(target = "quantityRange", expression = "java(formatQuantityRange(entity))")
    PriceScaleDto toDto(PriceScale entity);
    
    List<PriceScaleDto> toDtoList(List<PriceScale> entities);
    
    @Override
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true),
        @Mapping(target = "priceCondition", ignore = true)
    })
    PriceScale toEntity(PriceScaleDto dto);
    
    @Override
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true)
    })
    PriceScale requestToEntity(PriceScaleRequest request);
    
    @Override
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true),
        @Mapping(target = "priceCondition", ignore = true)
    })
    void updateEntity(PriceScaleDto dto, @MappingTarget PriceScale entity);
    
    @Override
    void updateEntityFromRequest(PriceScaleRequest request, @MappingTarget PriceScale entity);
    
    @Override
    PriceScaleResponse entityToResponse(PriceScale entity);
    
    List<PriceScaleResponse> toResponseList(List<PriceScale> entities);
    
    @Override
    PriceScaleRequest responseToRequest(PriceScaleResponse response);
    
    default String formatQuantityRange(PriceScale entity) {
        if (entity.getToQuantity() == null) {
            return entity.getFromQuantity() + "以上";
        } else {
            return entity.getFromQuantity() + "～" + entity.getToQuantity();
        }
    }
    
    default BigDecimal calculateDiscountPercentage(PriceScale entity) {
        if (entity.getPriceCondition() == null || entity.getPriceCondition().getBasePrice().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal basePrice = entity.getPriceCondition().getBasePrice();
        BigDecimal difference = basePrice.subtract(entity.getScalePrice());
        return difference.multiply(new BigDecimal("100")).divide(basePrice, 2, BigDecimal.ROUND_HALF_UP);
    }
    
    default BigDecimal calculateDiscountAmount(PriceScale entity) {
        if (entity.getPriceCondition() == null) {
            return BigDecimal.ZERO;
        }
        
        return entity.getPriceCondition().getBasePrice().subtract(entity.getScalePrice());
    }
} 