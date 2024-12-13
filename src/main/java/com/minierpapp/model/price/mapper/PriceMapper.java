package com.minierpapp.model.price.mapper;

import com.minierpapp.model.common.Status;
import com.minierpapp.model.price.*;
import com.minierpapp.model.price.dto.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    imports = {Status.class}
)
public interface PriceMapper {
    PriceMapper INSTANCE = Mappers.getMapper(PriceMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "status", expression = "java(Status.ACTIVE)")
    Price toEntity(PriceRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "price", ignore = true)
    PriceItem toEntity(PriceItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "price", ignore = true)
    PriceCustomerItem toEntity(PriceCustomerItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "price", ignore = true)
    PriceSupplierItem toEntity(PriceSupplierItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "price", ignore = true)
    PriceSupplierCustomerItem toEntity(PriceSupplierCustomerItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    PriceScale toEntity(PriceScaleRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "priceItems", ignore = true)
    @Mapping(target = "priceSupplierItems", ignore = true)
    @Mapping(target = "priceCustomerItems", ignore = true)
    @Mapping(target = "priceSupplierCustomerItems", ignore = true)
    void updateEntity(@MappingTarget Price price, PriceRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "priceScales", ignore = true)
    void updateEntity(@MappingTarget PriceItem priceItem, PriceItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "priceScales", ignore = true)
    void updateEntity(@MappingTarget PriceSupplierItem priceSupplierItem, PriceSupplierItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "priceScales", ignore = true)
    void updateEntity(@MappingTarget PriceCustomerItem priceCustomerItem, PriceCustomerItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "priceScales", ignore = true)
    void updateEntity(@MappingTarget PriceSupplierCustomerItem priceSupplierCustomerItem, PriceSupplierCustomerItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "price", ignore = true)
    void updateEntity(@MappingTarget PriceScale priceScale, PriceScaleRequest request);

    PriceResponse toResponse(Price price);
    PriceItemResponse toResponse(PriceItem priceItem);
    PriceSupplierItemResponse toResponse(PriceSupplierItem priceSupplierItem);
    PriceCustomerItemResponse toResponse(PriceCustomerItem priceCustomerItem);
    PriceSupplierCustomerItemResponse toResponse(PriceSupplierCustomerItem priceSupplierCustomerItem);
    PriceScaleResponse toResponse(PriceScale priceScale);

    @AfterMapping
    default void setPriceScales(@MappingTarget PriceItem priceItem, PriceItemRequest request) {
        if (request.getPriceScales() != null) {
            request.getPriceScales().forEach(scaleRequest -> {
                PriceScale scale = toEntity(scaleRequest);
                scale.setPriceItem(priceItem);
                priceItem.getPriceScales().add(scale);
            });
        }
    }

    @AfterMapping
    default void setPriceScales(@MappingTarget PriceSupplierItem priceSupplierItem, PriceSupplierItemRequest request) {
        if (request.getPriceScales() != null) {
            request.getPriceScales().forEach(scaleRequest -> {
                PriceScale scale = toEntity(scaleRequest);
                scale.setPriceSupplierItem(priceSupplierItem);
                priceSupplierItem.getPriceScales().add(scale);
            });
        }
    }

    @AfterMapping
    default void setPriceScales(@MappingTarget PriceCustomerItem priceCustomerItem, PriceCustomerItemRequest request) {
        if (request.getPriceScales() != null) {
            request.getPriceScales().forEach(scaleRequest -> {
                PriceScale scale = toEntity(scaleRequest);
                scale.setPriceCustomerItem(priceCustomerItem);
                priceCustomerItem.getPriceScales().add(scale);
            });
        }
    }

    @AfterMapping
    default void setPriceScales(@MappingTarget PriceSupplierCustomerItem priceSupplierCustomerItem, PriceSupplierCustomerItemRequest request) {
        if (request.getPriceScales() != null) {
            request.getPriceScales().forEach(scaleRequest -> {
                PriceScale scale = toEntity(scaleRequest);
                scale.setPriceSupplierCustomerItem(priceSupplierCustomerItem);
                priceSupplierCustomerItem.getPriceScales().add(scale);
            });
        }
    }
}