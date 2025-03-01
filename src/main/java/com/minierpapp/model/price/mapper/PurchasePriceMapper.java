package com.minierpapp.model.price.mapper;

import com.minierpapp.model.base.BaseMapper;
import com.minierpapp.model.price.dto.PurchasePriceDto;
import com.minierpapp.model.price.dto.PurchasePriceRequest;
import com.minierpapp.model.price.dto.PurchasePriceResponse;
import com.minierpapp.model.price.entity.PriceCondition;
import org.mapstruct.*;
import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring", uses = {PriceScaleMapper.class})
public interface PurchasePriceMapper extends BaseMapper<PriceCondition, PurchasePriceDto, PurchasePriceRequest, PurchasePriceResponse> {
    
    @Override
    @Mapping(target = "itemName", source = "item.itemName")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "currentlyValid", expression = "java(entity.isCurrentlyValid())")
    @Mapping(target = "expiringSoon", expression = "java(entity.getValidToDate().minusDays(30).isBefore(LocalDate.now()) && entity.getValidToDate().isAfter(LocalDate.now()))")
    @Mapping(target = "priceScales", source = "priceScales")
    PurchasePriceDto toDto(PriceCondition entity);
    
    List<PurchasePriceDto> toDtoList(List<PriceCondition> entities);
    
    @Override
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "priceType", constant = "PURCHASE"),
        @Mapping(target = "item", ignore = true),
        @Mapping(target = "customer", ignore = true),
        @Mapping(target = "supplier", ignore = true),
        @Mapping(target = "customerCode", ignore = true),
        @Mapping(target = "priceScales", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true)
    })
    PriceCondition toEntity(PurchasePriceDto dto);
    
    @Override
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "priceType", constant = "PURCHASE"),
        @Mapping(target = "item", ignore = true),
        @Mapping(target = "customer", ignore = true),
        @Mapping(target = "supplier", ignore = true),
        @Mapping(target = "customerCode", ignore = true),
        @Mapping(target = "priceScales", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true)
    })
    PriceCondition requestToEntity(PurchasePriceRequest request);
    
    @Override
    @Mapping(target = "itemName", source = "item.itemName")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "currentlyValid", expression = "java(entity.isCurrentlyValid())")
    @Mapping(target = "expiringSoon", expression = "java(entity.getValidToDate().minusDays(30).isBefore(LocalDate.now()) && entity.getValidToDate().isAfter(LocalDate.now()))")
    @Mapping(target = "validityStatus", expression = "java(getValidityStatus(entity))")
    @Mapping(target = "priceScales", source = "priceScales")
    PurchasePriceResponse entityToResponse(PriceCondition entity);
    
    List<PurchasePriceResponse> toResponseList(List<PriceCondition> entities);
    
    @Override
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "priceType", constant = "PURCHASE"),
        @Mapping(target = "item", ignore = true),
        @Mapping(target = "customer", ignore = true),
        @Mapping(target = "supplier", ignore = true),
        @Mapping(target = "customerCode", ignore = true),
        @Mapping(target = "priceScales", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true)
    })
    void updateEntity(PurchasePriceDto dto, @MappingTarget PriceCondition entity);
    
    @Override
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "priceType", constant = "PURCHASE"),
        @Mapping(target = "item", ignore = true),
        @Mapping(target = "customer", ignore = true),
        @Mapping(target = "supplier", ignore = true),
        @Mapping(target = "customerCode", ignore = true),
        @Mapping(target = "priceScales", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true)
    })
    void updateEntityFromRequest(PurchasePriceRequest request, @MappingTarget PriceCondition entity);
    
    @Override
    default PurchasePriceRequest responseToRequest(PurchasePriceResponse response) {
        PurchasePriceRequest request = new PurchasePriceRequest();
        request.setId(response.getId());
        request.setItemId(response.getItemId());
        request.setItemCode(response.getItemCode());
        request.setSupplierId(response.getSupplierId());
        request.setSupplierCode(response.getSupplierCode());
        request.setBasePrice(response.getBasePrice());
        request.setCurrencyCode(response.getCurrencyCode());
        request.setValidFromDate(response.getValidFromDate());
        request.setValidToDate(response.getValidToDate());
        request.setStatus(response.getStatus());
        return request;
    }
    
    default String getValidityStatus(PriceCondition entity) {
        LocalDate today = LocalDate.now();
        if (entity.getValidToDate().isBefore(today)) {
            return "期限切れ";
        } else if (entity.getValidToDate().minusDays(30).isBefore(today)) {
            return "期限切れ間近";
        } else {
            return "有効";
        }
    }
} 