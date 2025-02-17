package com.minierpapp.model.supplier.mapper;

import com.minierpapp.model.base.BaseMapper;
import com.minierpapp.model.supplier.Supplier;
import com.minierpapp.model.supplier.dto.SupplierDto;
import com.minierpapp.model.supplier.dto.SupplierRequest;
import com.minierpapp.model.supplier.dto.SupplierResponse;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.Collections;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {Collections.class}
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
}