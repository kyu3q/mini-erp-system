package com.minierpapp.model.warehouse.mapper;

import com.minierpapp.model.base.BaseMapper;
import com.minierpapp.model.warehouse.Warehouse;
import com.minierpapp.model.warehouse.dto.WarehouseDto;
import com.minierpapp.model.warehouse.dto.WarehouseRequest;
import com.minierpapp.model.warehouse.dto.WarehouseResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.Collections;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {Collections.class}
)
public interface WarehouseMapper extends BaseMapper<Warehouse, WarehouseDto, WarehouseRequest, WarehouseResponse> {
    @Override
    Warehouse toEntity(WarehouseDto dto);

    @Override
    WarehouseDto toDto(Warehouse entity);

    @Override
    Warehouse requestToEntity(WarehouseRequest request);

    @Override
    WarehouseResponse entityToResponse(Warehouse entity);

    @Override
    void updateEntityFromRequest(WarehouseRequest request, @MappingTarget Warehouse entity);

    @Override
    void updateEntity(WarehouseDto dto, @MappingTarget Warehouse entity);

    @Override
    WarehouseRequest responseToRequest(WarehouseResponse response);
}