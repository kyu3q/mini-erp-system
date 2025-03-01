package com.minierpapp.model.price.mapper;

import com.minierpapp.model.base.BaseMapper;
import com.minierpapp.model.price.dto.PriceConditionDto;
import com.minierpapp.model.price.dto.PriceConditionRequest;
import com.minierpapp.model.price.dto.PriceConditionResponse;
import com.minierpapp.model.price.entity.PriceCondition;
import com.minierpapp.model.price.dto.PriceScaleRequest;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {PriceScaleMapper.class})
public interface PriceConditionMapper extends BaseMapper<PriceCondition, PriceConditionDto, PriceConditionRequest, PriceConditionResponse> {
    
    @Override
    @Mapping(target = "itemName", source = "item.itemName")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "expiringSoon", expression = "java(entity.getValidToDate().minusDays(30).isBefore(java.time.LocalDate.now()) && entity.getValidToDate().isAfter(java.time.LocalDate.now()))")
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
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "expiringSoon", expression = "java(entity.getValidToDate().minusDays(30).isBefore(java.time.LocalDate.now()) && entity.getValidToDate().isAfter(java.time.LocalDate.now()))")
    @Mapping(target = "expired", expression = "java(entity.getValidToDate().isBefore(java.time.LocalDate.now()))")
    PriceConditionResponse entityToResponse(PriceCondition entity);
    
    List<PriceConditionResponse> toResponseList(List<PriceCondition> entities);
    
    @Override
    default PriceConditionRequest responseToRequest(PriceConditionResponse response) {
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

    default PriceConditionRequest toRequest(PriceCondition entity) {
        if (entity == null) {
            return null;
        }
        
        PriceConditionRequest request = new PriceConditionRequest();
        request.setId(entity.getId());
        request.setItemId(entity.getItem().getId());
        request.setCustomerId(entity.getCustomer() != null ? entity.getCustomer().getId() : null);
        request.setSupplierId(entity.getSupplier() != null ? entity.getSupplier().getId() : null);
        request.setBasePrice(entity.getBasePrice());
        request.setCurrencyCode(entity.getCurrencyCode());
        request.setValidFromDate(entity.getValidFromDate());
        request.setValidToDate(entity.getValidToDate());
        request.setStatus(entity.getStatus());
        
        if (entity.getPriceScales() != null) {
            request.setPriceScales(entity.getPriceScales().stream()
                .map(scale -> {
                    PriceScaleRequest scaleRequest = new PriceScaleRequest();
                    scaleRequest.setQuantity(scale.getFromQuantity());
                    scaleRequest.setPrice(scale.getScalePrice());
                    return scaleRequest;
                })
                .collect(Collectors.toList()));
        }
        
        return request;
    }

    /**
     * レスポンスリストからエンティティリストへの変換
     */
    default List<PriceCondition> responsesToEntities(List<PriceConditionResponse> responses) {
        if (responses == null) {
            return null;
        }
        return responses.stream()
            .map(this::responseToEntity)
            .collect(Collectors.toList());
    }

    /**
     * レスポンスからエンティティへの変換
     */
    @Mappings({
        @Mapping(target = "deleted", constant = "false"),
        @Mapping(target = "item", ignore = true),
        @Mapping(target = "customer", ignore = true),
        @Mapping(target = "supplier", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true)
    })
    PriceCondition responseToEntity(PriceConditionResponse response);
} 