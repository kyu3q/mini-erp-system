package com.minierpapp.model.price.mapper;

import com.minierpapp.model.base.BaseMapper;
import com.minierpapp.model.price.dto.SalesPriceDto;
import com.minierpapp.model.price.dto.SalesPriceRequest;
import com.minierpapp.model.price.dto.SalesPriceResponse;
import com.minierpapp.model.price.entity.PriceCondition;
import org.mapstruct.*;
import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring", uses = {PriceScaleMapper.class})
public interface SalesPriceMapper extends BaseMapper<PriceCondition, SalesPriceDto, SalesPriceRequest, SalesPriceResponse> {
    
    @Override
    @Mapping(target = "itemName", source = "item.itemName")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "customerId", source = "customer.id")
    // @Mapping(target = "currentlyValid", expression = "java(entity.isCurrentlyValid())")
    // @Mapping(target = "expiringSoon", expression = "java(entity.getValidToDate().minusDays(30).isBefore(LocalDate.now()) && entity.getValidToDate().isAfter(LocalDate.now()))")
    @Mapping(target = "priceScales", source = "priceScales")
    SalesPriceDto toDto(PriceCondition entity);
    
    List<SalesPriceDto> toDtoList(List<PriceCondition> entities);
    
    @Override
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "priceType", constant = "SALES"),
        @Mapping(target = "item", ignore = true),
        @Mapping(target = "customer", ignore = true),
        @Mapping(target = "supplier", ignore = true),
        @Mapping(target = "supplierCode", ignore = true),
        @Mapping(target = "priceScales", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true)
    })
    PriceCondition toEntity(SalesPriceDto dto);
    
    @Override
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "priceType", constant = "SALES"),
        @Mapping(target = "item", ignore = true),
        @Mapping(target = "customer", ignore = true),
        @Mapping(target = "supplier", ignore = true),
        @Mapping(target = "supplierCode", ignore = true),
        @Mapping(target = "priceScales", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true)
    })
    PriceCondition requestToEntity(SalesPriceRequest request);
    
    @Override
    @Mapping(target = "itemName", source = "item.itemName")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "customerId", source = "customer.id")
    // @Mapping(target = "currentlyValid", expression = "java(entity.isCurrentlyValid())")
    // @Mapping(target = "expiringSoon", expression = "java(entity.getValidToDate().minusDays(30).isBefore(LocalDate.now()) && entity.getValidToDate().isAfter(LocalDate.now()))")
    // @Mapping(target = "validityStatus", expression = "java(getValidityStatus(entity))")
    @Mapping(target = "priceScales", source = "priceScales")
    SalesPriceResponse entityToResponse(PriceCondition entity);
    
    List<SalesPriceResponse> toResponseList(List<PriceCondition> entities);
    
    @Override
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "priceType", constant = "SALES"),
        @Mapping(target = "item", ignore = true),
        @Mapping(target = "customer", ignore = true),
        @Mapping(target = "supplier", ignore = true),
        @Mapping(target = "supplierCode", ignore = true),
        @Mapping(target = "priceScales", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true)
    })
    void updateEntity(SalesPriceDto dto, @MappingTarget PriceCondition entity);
    
    @Override
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "priceType", constant = "SALES"),
        @Mapping(target = "item", ignore = true),
        @Mapping(target = "customer", ignore = true),
        @Mapping(target = "supplier", ignore = true),
        @Mapping(target = "supplierCode", ignore = true),
        @Mapping(target = "priceScales", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true)
    })
    void updateEntityFromRequest(SalesPriceRequest request, @MappingTarget PriceCondition entity);
    
    @Override
    default SalesPriceRequest responseToRequest(SalesPriceResponse response) {
        SalesPriceRequest request = new SalesPriceRequest();
        request.setId(response.getId());
        request.setItemId(response.getItemId());
        request.setItemCode(response.getItemCode());
        request.setCustomerId(response.getCustomerId());
        request.setCustomerCode(response.getCustomerCode());
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