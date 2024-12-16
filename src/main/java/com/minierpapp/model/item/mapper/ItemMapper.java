package com.minierpapp.model.item.mapper;

import com.minierpapp.model.common.mapper.BaseMapper;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.item.dto.ItemDto;
import com.minierpapp.model.item.dto.ItemRequest;
import com.minierpapp.model.item.dto.ItemResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ItemMapper implements BaseMapper<Item, ItemDto, ItemRequest, ItemResponse> {

    @Override
    public ItemResponse toResponse(Item item) {
        if (item == null) {
            return null;
        }

        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setItemCode(item.getItemCode());
        response.setItemName(item.getItemName());
        response.setDescription(item.getDescription());
        response.setUnit(item.getUnit());
        response.setStatus(item.getStatus());
        response.setMinimumStock(item.getMinimumStock());
        response.setMaximumStock(item.getMaximumStock());
        response.setReorderPoint(item.getReorderPoint());
        response.setCreatedAt(item.getCreatedAt());
        response.setCreatedBy(item.getCreatedBy());
        response.setUpdatedAt(item.getUpdatedAt());
        response.setUpdatedBy(item.getUpdatedBy());
        return response;
    }

    @Override
    public Item toEntity(ItemRequest request) {
        if (request == null) {
            return null;
        }

        Item item = new Item();
        item.setItemCode(request.getItemCode());
        item.setItemName(request.getItemName());
        item.setDescription(request.getDescription());
        item.setUnit(request.getUnit());
        item.setStatus(request.getStatus());
        item.setMinimumStock(request.getMinimumStock());
        item.setMaximumStock(request.getMaximumStock());
        item.setReorderPoint(request.getReorderPoint());
        item.setDeleted(false);
        item.setCreatedAt(LocalDateTime.now());
        return item;
    }

    @Override
    public void updateEntity(ItemRequest request, Item item) {
        if (request == null || item == null) {
            return;
        }

        item.setItemCode(request.getItemCode());
        item.setItemName(request.getItemName());
        item.setDescription(request.getDescription());
        item.setUnit(request.getUnit());
        item.setStatus(request.getStatus());
        item.setMinimumStock(request.getMinimumStock());
        item.setMaximumStock(request.getMaximumStock());
        item.setReorderPoint(request.getReorderPoint());
    }

    @Override
    public ItemDto toDto(Item entity) {
        if (entity == null) {
            return null;
        }

        ItemDto dto = new ItemDto();
        dto.setId(entity.getId());
        dto.setItemCode(entity.getItemCode());
        dto.setItemName(entity.getItemName());
        dto.setDescription(entity.getDescription());
        dto.setUnit(entity.getUnit());
        dto.setStatus(entity.getStatus());
        dto.setMinimumStock(entity.getMinimumStock());
        dto.setMaximumStock(entity.getMaximumStock());
        dto.setReorderPoint(entity.getReorderPoint());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }

    @Override
    public ItemRequest toRequest(ItemDto dto) {
        if (dto == null) {
            return null;
        }

        ItemRequest request = new ItemRequest();
        request.setId(dto.getId());
        request.setItemCode(dto.getItemCode());
        request.setItemName(dto.getItemName());
        request.setDescription(dto.getDescription());
        request.setUnit(dto.getUnit());
        request.setStatus(dto.getStatus());
        request.setMinimumStock(dto.getMinimumStock());
        request.setMaximumStock(dto.getMaximumStock());
        request.setReorderPoint(dto.getReorderPoint());
        return request;
    }
}