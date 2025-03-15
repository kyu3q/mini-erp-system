package com.minierpapp.model.price.mapper;

import com.minierpapp.model.base.BaseMapper;
import com.minierpapp.model.price.dto.PurchasePriceDto;
import com.minierpapp.model.price.dto.PurchasePriceRequest;
import com.minierpapp.model.price.dto.PurchasePriceResponse;
import com.minierpapp.model.price.entity.PurchasePrice;
import org.mapstruct.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {PriceScaleMapper.class}, imports = {LocalDate.class})
public interface PurchasePriceMapper extends BaseMapper<PurchasePrice, PurchasePriceDto, PurchasePriceRequest, PurchasePriceResponse> {
    
    @Override
    @Mapping(target = "priceType", constant = "PURCHASE")
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    PurchasePrice toEntity(PurchasePriceDto dto);

    @Override
    @Mapping(target = "itemName", source = "item.itemName")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "customerName", source = "customer.name")
    PurchasePriceDto toDto(PurchasePrice entity);
    
    @Override
    @Mapping(target = "priceType", constant = "PURCHASE")
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    PurchasePrice requestToEntity(PurchasePriceRequest request);

    @Override
    @Mapping(target = "itemName", source = "item.itemName")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "priceScales", expression = "java(priceScaleMapper.toResponseList(entity.getPriceScales()))")
    PurchasePriceResponse entityToResponse(PurchasePrice entity);
    
    @Override
    @Mapping(target = "priceType", constant = "PURCHASE")
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "priceScales", ignore = true)
    @Mapping(target = "itemId", source = "itemId")
    @Mapping(target = "supplierId", source = "supplierId")
    void updateEntity(PurchasePriceDto dto, @MappingTarget PurchasePrice entity);
    
    @Override
    @Mapping(target = "priceType", constant = "SALES")
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntityFromRequest(PurchasePriceRequest request, @MappingTarget PurchasePrice entity);
    
    @Override
    default PurchasePriceRequest responseToRequest(PurchasePriceResponse response) {
        if (response == null) {
            return null;
        }
        
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
    
    default String getValidityStatus(PurchasePrice entity) {
        return "有効";
    }
    
    /**
     * レスポンスのリストからエンティティのリストに変換
     */
    default List<PurchasePrice> responsesToEntities(List<PurchasePriceResponse> responses) {
        if (responses == null) {
            return null;
        }
        
        return responses.stream()
            .map(response -> {
                PurchasePrice entity = new PurchasePrice();
                entity.setId(response.getId());
                entity.setItemId(response.getItemId());
                entity.setItemCode(response.getItemCode());
                entity.setSupplierId(response.getSupplierId());
                entity.setSupplierCode(response.getSupplierCode());
                entity.setCustomerId(response.getCustomerId());
                entity.setCustomerCode(response.getCustomerCode());
                entity.setBasePrice(response.getBasePrice());
                entity.setCurrencyCode(response.getCurrencyCode());
                entity.setValidFromDate(response.getValidFromDate());
                entity.setValidToDate(response.getValidToDate());
                entity.setStatus(response.getStatus());
                return entity;
            })
            .collect(Collectors.toList());
    }

    // expiringSoonを判定するヘルパーメソッド
    default boolean isExpiringSoon(PurchasePrice entity) {
        if (entity.getValidToDate() == null) {
            return false;
        }
        LocalDate now = LocalDate.now();
        LocalDate thirtyDaysLater = now.plusDays(30);
        return entity.getValidToDate().isAfter(now) && 
               entity.getValidToDate().isBefore(thirtyDaysLater);
    }

    default List<PurchasePriceResponse> toResponseList(List<PurchasePrice> entities) {
        return entities.stream()
            .map(this::entityToResponse)
            .collect(Collectors.toList());
    }

    default List<PurchasePriceDto> toDtoList(List<PurchasePrice> entities) {
        return entities.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
}