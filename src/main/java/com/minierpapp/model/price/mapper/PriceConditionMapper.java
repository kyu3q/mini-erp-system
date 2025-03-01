package com.minierpapp.model.price.mapper;

import com.minierpapp.model.base.BaseMapper;
import com.minierpapp.model.price.dto.PriceConditionDto;
import com.minierpapp.model.price.dto.PriceConditionRequest;
import com.minierpapp.model.price.dto.PriceConditionResponse;
import com.minierpapp.model.price.entity.PriceCondition;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", uses = {PriceScaleMapper.class})
public interface PriceConditionMapper extends BaseMapper<PriceCondition, PriceConditionDto, PriceConditionRequest, PriceConditionResponse> {
    
    @Override
    @Mapping(target = "itemName", source = "item.itemName")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "supplierName", source = "supplier.name")
    // @Mapping(target = "currentlyValid", expression = "java(entity.isCurrentlyValid())")
    // @Mapping(target = "expiringSoon", expression = "java(entity.getValidToDate() != null && entity.getValidToDate().minusDays(30).isBefore(LocalDate.now()) && entity.getValidToDate().isAfter(LocalDate.now()))")
    PriceConditionDto toDto(PriceCondition entity);
    
    List<PriceConditionDto> toDtoList(List<PriceCondition> entities);
    
    @Override
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true),
        @Mapping(target = "item", ignore = true),
        @Mapping(target = "customer", ignore = true),
        @Mapping(target = "supplier", ignore = true),
        @Mapping(target = "priceScales", ignore = true)
    })
    PriceCondition toEntity(PriceConditionDto dto);
    
    @Override
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true),
        @Mapping(target = "item", ignore = true),
        @Mapping(target = "customer", ignore = true),
        @Mapping(target = "supplier", ignore = true),
        @Mapping(target = "priceScales", ignore = true)
    })
    PriceCondition requestToEntity(PriceConditionRequest request);
    
    @Override
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true),
        @Mapping(target = "item", ignore = true),
        @Mapping(target = "customer", ignore = true),
        @Mapping(target = "supplier", ignore = true),
        @Mapping(target = "priceScales", ignore = true)
    })
    void updateEntity(PriceConditionDto dto, @MappingTarget PriceCondition entity);
    
    @Override
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true),
        @Mapping(target = "item", ignore = true),
        @Mapping(target = "customer", ignore = true),
        @Mapping(target = "supplier", ignore = true),
        @Mapping(target = "priceScales", ignore = true)
    })
    void updateEntityFromRequest(PriceConditionRequest request, @MappingTarget PriceCondition entity);
    
    @Override
    @Mapping(target = "itemName", source = "item.itemName")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "supplierName", source = "supplier.name")
    // @Mapping(target = "currentlyValid", expression = "java(entity.isCurrentlyValid())")
    // @Mapping(target = "expiringSoon", expression = "java(entity.getValidToDate() != null && entity.getValidToDate().minusDays(30).isBefore(LocalDate.now()) && entity.getValidToDate().isAfter(LocalDate.now()))")
    // @Mapping(target = "partnerName", expression = "java(entity.getCustomer() != null ? entity.getCustomer().getName() : (entity.getSupplier() != null ? entity.getSupplier().getName() : null))")
    // @Mapping(target = "partnerType", expression = "java(entity.getCustomer() != null ? \"顧客\" : (entity.getSupplier() != null ? \"仕入先\" : null))")
    PriceConditionResponse entityToResponse(PriceCondition entity);
    
    List<PriceConditionResponse> toResponseList(List<PriceCondition> entities);
    
    @Override
    default PriceConditionRequest responseToRequest(PriceConditionResponse response) {
        if (response == null) {
            return null;
        }
        
        PriceConditionRequest request = new PriceConditionRequest();
        request.setId(response.getId());
        request.setItemId(response.getItemId());
        request.setItemCode(response.getItemCode());
        request.setCustomerId(response.getCustomerId());
        request.setCustomerCode(response.getCustomerCode());
        request.setSupplierId(response.getSupplierId());
        request.setSupplierCode(response.getSupplierCode());
        request.setBasePrice(response.getBasePrice());
        request.setCurrencyCode(response.getCurrencyCode());
        request.setValidFromDate(response.getValidFromDate());
        request.setValidToDate(response.getValidToDate());
        request.setStatus(response.getStatus());
        return request;
    }
    
    // エンティティのリストをレスポンスのリストに変換するヘルパーメソッド
    default List<PriceCondition> responsesToEntities(List<PriceConditionResponse> responses) {
        if (responses == null) {
            return null;
        }
        
        return responses.stream()
            .map(response -> {
                PriceCondition entity = new PriceCondition();
                entity.setId(response.getId());
                entity.setItemId(response.getItemId());
                entity.setItemCode(response.getItemCode());
                entity.setCustomerId(response.getCustomerId());
                entity.setCustomerCode(response.getCustomerCode());
                entity.setSupplierId(response.getSupplierId());
                entity.setSupplierCode(response.getSupplierCode());
                entity.setBasePrice(response.getBasePrice());
                entity.setCurrencyCode(response.getCurrencyCode());
                entity.setValidFromDate(response.getValidFromDate());
                entity.setValidToDate(response.getValidToDate());
                entity.setStatus(response.getStatus());
                return entity;
            })
            .toList();
    }
} 