package com.minierpapp.model.supplier.mapper;

import com.minierpapp.model.common.mapper.BaseMapper;
import com.minierpapp.model.supplier.Supplier;
import com.minierpapp.model.supplier.dto.SupplierDto;
import com.minierpapp.model.supplier.dto.SupplierRequest;
import com.minierpapp.model.supplier.dto.SupplierResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SupplierMapper extends BaseMapper<Supplier, SupplierDto, SupplierRequest, SupplierResponse> {
    @Override
    SupplierDto toDto(Supplier supplier);

    @Override
    SupplierResponse toResponse(Supplier supplier);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Supplier toEntity(SupplierRequest request);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(SupplierRequest request, @MappingTarget Supplier supplier);

    @Override
    SupplierRequest toRequest(SupplierDto dto);
}