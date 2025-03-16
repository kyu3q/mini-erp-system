package com.minierpapp.model.price.mapper;

import com.minierpapp.model.base.BaseMapper;
import com.minierpapp.model.price.dto.PriceScaleRequest;
import com.minierpapp.model.price.dto.SalesPriceDto;
import com.minierpapp.model.price.dto.SalesPriceRequest;
import com.minierpapp.model.price.dto.SalesPriceResponse;
import com.minierpapp.model.price.entity.PriceScale;
import com.minierpapp.model.price.entity.SalesPrice;
import org.mapstruct.*;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {PriceScaleMapper.class})
public interface SalesPriceMapper extends BaseMapper<SalesPrice, SalesPriceDto, SalesPriceRequest, SalesPriceResponse> {
    
    @Override
    @Mapping(target = "itemName", source = "item.itemName")
    @Mapping(target = "customerName", source = "customer.name")
    SalesPriceDto toDto(SalesPrice entity);
    
    @Override
    @Mappings({
        @Mapping(target = "priceType", constant = "SALES"),
        @Mapping(target = "item", ignore = true),
        @Mapping(target = "customer", ignore = true),
        @Mapping(target = "supplier", ignore = true),
        @Mapping(target = "supplierCode", ignore = true),
        @Mapping(target = "supplierId", ignore = true)
    })
    SalesPrice toEntity(SalesPriceDto dto);
    
    @Override
    @Mappings({
        @Mapping(target = "priceType", constant = "SALES"),
        @Mapping(target = "item", ignore = true),
        @Mapping(target = "customer", ignore = true),
        @Mapping(target = "supplier", ignore = true),
        @Mapping(target = "supplierCode", ignore = true),
        @Mapping(target = "supplierId", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true)
    })
    SalesPrice requestToEntity(SalesPriceRequest request);
    
    @Override
    @Mapping(target = "itemName", source = "item.itemName")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "priceScales", expression = "java(priceScaleMapper.toResponseList(entity.getPriceScales()))")
    SalesPriceResponse entityToResponse(SalesPrice entity);
    
    @Override
    @Mappings({
        @Mapping(target = "priceType", constant = "SALES"),
        @Mapping(target = "item", ignore = true),
        @Mapping(target = "customer", ignore = true),
        @Mapping(target = "supplier", ignore = true),
        @Mapping(target = "supplierCode", ignore = true),
        @Mapping(target = "supplierId", ignore = true)
    })
    void updateEntity(SalesPriceDto dto, @MappingTarget SalesPrice entity);
    
    @Override
    @Mappings({
        @Mapping(target = "priceType", constant = "SALES"),
        @Mapping(target = "item", ignore = true),
        @Mapping(target = "customer", ignore = true),
        @Mapping(target = "supplier", ignore = true),
        @Mapping(target = "supplierCode", ignore = true),
        @Mapping(target = "supplierId", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true)
    })
    void updateEntityFromRequest(SalesPriceRequest request, @MappingTarget SalesPrice entity);
    
    @Override
    default SalesPriceRequest responseToRequest(SalesPriceResponse response) {
        if (response == null) {
            return null;
        }
        
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
        if (response.getPriceScales() != null) {
            request.setPriceScales(response.getPriceScales().stream()
                .map(scaleResponse -> {
                    PriceScaleRequest scaleRequest = new PriceScaleRequest();
                    scaleRequest.setFromQuantity(scaleResponse.getFromQuantity());
                    scaleRequest.setToQuantity(scaleResponse.getToQuantity());
                    scaleRequest.setScalePrice(scaleResponse.getScalePrice());
                    return scaleRequest;
                })
                .collect(Collectors.toList()));
        }
        return request;
    }
    
    default String getValidityStatus(SalesPrice entity) {
        return "有効";
    }
    
    /**
     * レスポンスのリストからエンティティのリストに変換
     */
    default List<SalesPrice> responsesToEntities(List<SalesPriceResponse> responses) {
        if (responses == null) {
            return null;
        }
        
        return responses.stream()
            .map(response -> {
                SalesPrice entity = new SalesPrice();
                entity.setId(response.getId());
                entity.setItemId(response.getItemId());
                entity.setItemCode(response.getItemCode());
                entity.setCustomerId(response.getCustomerId());
                entity.setCustomerCode(response.getCustomerCode());
                entity.setBasePrice(response.getBasePrice());
                entity.setCurrencyCode(response.getCurrencyCode());
                entity.setValidFromDate(response.getValidFromDate());
                entity.setValidToDate(response.getValidToDate());
                entity.setStatus(response.getStatus());
                if (response.getPriceScales() != null) {
                    List<PriceScale> scales = response.getPriceScales().stream()
                        .map(scaleResponse -> {
                            PriceScale scale = new PriceScale();
                            scale.setFromQuantity(scaleResponse.getFromQuantity());
                            scale.setToQuantity(scaleResponse.getToQuantity());
                            scale.setScalePrice(scaleResponse.getScalePrice());
                            scale.setPrice(entity);
                            return scale;
                        })
                        .collect(Collectors.toList());
                    entity.setPriceScales(scales);
                }
                return entity;
            })
            .collect(Collectors.toList());
    }

    default List<SalesPriceResponse> toResponseList(List<SalesPrice> entities) {
        return entities.stream()
            .map(this::entityToResponse)
            .collect(Collectors.toList());
    }

    default List<SalesPriceDto> toDtoList(List<SalesPrice> entities) {
        return entities.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
}