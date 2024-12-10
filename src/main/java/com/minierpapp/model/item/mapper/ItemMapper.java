package com.minierpapp.model.item.mapper;

import com.minierpapp.model.item.Item;
import com.minierpapp.model.item.dto.ItemRequest;
import com.minierpapp.model.item.dto.ItemResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ItemMapper {

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

    public void updateEntityFromRequest(ItemRequest request, Item item) {
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
}