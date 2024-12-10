package com.minierpapp.model.supplier.mapper;

import com.minierpapp.model.supplier.Supplier;
import com.minierpapp.model.supplier.dto.SupplierDto;
import com.minierpapp.model.supplier.dto.SupplierRequest;
import com.minierpapp.model.supplier.dto.SupplierResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SupplierMapper {
    SupplierMapper INSTANCE = Mappers.getMapper(SupplierMapper.class);

    SupplierDto toDto(Supplier supplier);
    SupplierResponse toResponse(Supplier supplier);
    Supplier toEntity(SupplierRequest request);
    void updateEntity(SupplierRequest request, @MappingTarget Supplier supplier);
}