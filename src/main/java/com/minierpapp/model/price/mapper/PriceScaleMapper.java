package com.minierpapp.model.price.mapper;

import com.minierpapp.model.base.BaseMapper;
import com.minierpapp.model.price.dto.PriceScaleDto;
import com.minierpapp.model.price.dto.PriceScaleRequest;
import com.minierpapp.model.price.dto.PriceScaleResponse;
import com.minierpapp.model.price.entity.PriceScale;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PriceScaleMapper extends BaseMapper<PriceScale, PriceScaleDto, PriceScaleRequest, PriceScaleResponse> {
    
    @Mapping(target = "priceId", source = "price.id")
    PriceScaleDto toDto(PriceScale entity);
    
    List<PriceScaleDto> toDtoList(List<PriceScale> entities);
    
    @Mapping(target = "price", ignore = true)
    PriceScale toEntity(PriceScaleDto dto);
    
    List<PriceScale> toEntityList(List<PriceScaleDto> dtos);
    
    @Override
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true),
        @Mapping(target = "price", ignore = true)
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
} 