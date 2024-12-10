package com.minierpapp.model.stock.mapper;

import com.minierpapp.model.stock.Stock;
import com.minierpapp.model.stock.dto.StockDto;
import com.minierpapp.model.stock.dto.StockRequest;
import com.minierpapp.model.stock.dto.StockResponse;
import org.springframework.stereotype.Component;

@Component
public class StockMapper {

    public StockDto toDto(Stock stock) {
        return StockDto.builder()
                .id(stock.getId())
                .warehouseId(stock.getWarehouse().getId())
                .warehouseCode(stock.getWarehouse().getWarehouseCode())
                .warehouseName(stock.getWarehouse().getName())
                .productId(stock.getProduct().getId())
                .itemCode(stock.getProduct().getItemCode())
                .itemName(stock.getProduct().getItemName())
                .quantity(stock.getQuantity())
                .minimumQuantity(stock.getMinimumQuantity())
                .maximumQuantity(stock.getMaximumQuantity())
                .location(stock.getLocation())
                .notes(stock.getNotes())
                .build();
    }

    public Stock toEntity(StockRequest request) {
        Stock stock = new Stock();
        stock.setQuantity(request.getQuantity());
        stock.setMinimumQuantity(request.getMinimumQuantity());
        stock.setMaximumQuantity(request.getMaximumQuantity());
        stock.setLocation(request.getLocation());
        stock.setNotes(request.getNotes());
        return stock;
    }

    public StockResponse toResponse(StockDto dto) {
        return StockResponse.builder()
                .id(dto.getId())
                .warehouseId(dto.getWarehouseId())
                .warehouseCode(dto.getWarehouseCode())
                .warehouseName(dto.getWarehouseName())
                .productId(dto.getProductId())
                .productCode(dto.getProductCode())
                .productName(dto.getProductName())
                .quantity(dto.getQuantity())
                .minimumQuantity(dto.getMinimumQuantity())
                .maximumQuantity(dto.getMaximumQuantity())
                .location(dto.getLocation())
                .notes(dto.getNotes())
                .build();
    }
}