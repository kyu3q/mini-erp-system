package com.minierpapp.model.warehouse.mapper;

import com.minierpapp.model.warehouse.Warehouse;
import com.minierpapp.model.warehouse.dto.WarehouseDto;
import com.minierpapp.model.warehouse.dto.WarehouseRequest;
import com.minierpapp.model.warehouse.dto.WarehouseResponse;
import org.springframework.stereotype.Component;

@Component
public class WarehouseMapper {

    public WarehouseDto toDto(Warehouse warehouse) {
        return WarehouseDto.builder()
                .id(warehouse.getId())
                .warehouseCode(warehouse.getWarehouseCode())
                .name(warehouse.getName())
                .address(warehouse.getAddress())
                .capacity(warehouse.getCapacity())
                .status(warehouse.getStatus())
                .description(warehouse.getDescription())
                .build();
    }

    public Warehouse toEntity(WarehouseRequest request) {
        Warehouse warehouse = new Warehouse();
        warehouse.setWarehouseCode(request.getWarehouseCode());
        warehouse.setName(request.getName());
        warehouse.setAddress(request.getAddress());
        warehouse.setCapacity(request.getCapacity());
        warehouse.setDescription(request.getDescription());
        return warehouse;
    }

    public WarehouseResponse toResponse(WarehouseDto dto) {
        return WarehouseResponse.builder()
                .id(dto.getId())
                .warehouseCode(dto.getWarehouseCode())
                .name(dto.getName())
                .address(dto.getAddress())
                .capacity(dto.getCapacity())
                .status(dto.getStatus())
                .description(dto.getDescription())
                .build();
    }
}