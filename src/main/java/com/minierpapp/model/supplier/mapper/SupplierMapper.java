package com.minierpapp.model.supplier.mapper;

import com.minierpapp.model.supplier.Supplier;
import com.minierpapp.model.supplier.dto.SupplierDto;
import com.minierpapp.model.supplier.dto.SupplierRequest;
import com.minierpapp.model.supplier.dto.SupplierResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    SupplierDto toDto(Supplier supplier);
    SupplierResponse toResponse(Supplier supplier);
    Supplier toEntity(SupplierRequest request);
    void updateEntity(SupplierRequest request, @MappingTarget Supplier supplier);
}