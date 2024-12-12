package com.minierpapp.model.price.mapper;

import com.minierpapp.model.price.*;
import com.minierpapp.model.price.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PriceMapper {
    PriceMapper INSTANCE = Mappers.getMapper(PriceMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Price toEntity(PriceRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PriceItem toEntity(PriceItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PriceCustomerItem toEntity(PriceCustomerItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PriceSupplierCustomerItem toEntity(PriceSupplierCustomerItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PriceScale toEntity(PriceScaleRequest request);

    void updateEntity(@MappingTarget Price price, PriceRequest request);
    void updateEntity(@MappingTarget PriceItem priceItem, PriceItemRequest request);
    void updateEntity(@MappingTarget PriceCustomerItem priceCustomerItem, PriceCustomerItemRequest request);
    void updateEntity(@MappingTarget PriceSupplierCustomerItem priceSupplierCustomerItem, PriceSupplierCustomerItemRequest request);
    void updateEntity(@MappingTarget PriceScale priceScale, PriceScaleRequest request);
}