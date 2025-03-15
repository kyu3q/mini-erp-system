package com.minierpapp.model.supplier.mapper;

import com.minierpapp.model.base.BaseMapper;
import com.minierpapp.model.supplier.Supplier;
import com.minierpapp.model.supplier.dto.SupplierDto;
import com.minierpapp.model.supplier.dto.SupplierRequest;
import com.minierpapp.model.supplier.dto.SupplierResponse;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SupplierMapper extends BaseMapper<Supplier, SupplierDto, SupplierRequest, SupplierResponse> {
    @Override
    Supplier toEntity(SupplierDto dto);

    @Override
    SupplierDto toDto(Supplier entity);

    @Override
    Supplier requestToEntity(SupplierRequest request);

    @Override
    SupplierResponse entityToResponse(Supplier entity);

    @Override
    void updateEntityFromRequest(SupplierRequest request, @MappingTarget Supplier entity);

    @Override
    void updateEntity(SupplierDto dto, @MappingTarget Supplier entity);

    @Override
    SupplierRequest responseToRequest(SupplierResponse response);

    default List<SupplierResponse> toResponseList(List<Supplier> entities) {
        return entities.stream()
            .map(this::entityToResponse)
            .collect(Collectors.toList());
    }

    default List<SupplierDto> toDtoList(List<Supplier> entities) {
        return entities.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
}