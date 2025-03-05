package com.minierpapp.model.price.mapper;

import com.minierpapp.model.base.BaseMapper;
import com.minierpapp.model.price.dto.PriceScaleDto;
import com.minierpapp.model.price.dto.PriceScaleRequest;
import com.minierpapp.model.price.dto.PriceScaleResponse;
import com.minierpapp.model.price.entity.PriceScale;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.Collections;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {Collections.class}
)
public interface PriceScaleMapper extends BaseMapper<PriceScale, PriceScaleDto, PriceScaleRequest, PriceScaleResponse> {
    
    @Override
    PriceScaleDto toDto(PriceScale entity);
    
    List<PriceScaleDto> toDtoList(List<PriceScale> entities);
    
    @Override
    PriceScale toEntity(PriceScaleDto dto);
    
    List<PriceScale> toEntityList(List<PriceScaleDto> dtos);
    
    @Override
    PriceScale requestToEntity(PriceScaleRequest request);

    @Override
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