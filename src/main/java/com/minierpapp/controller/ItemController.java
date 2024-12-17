package com.minierpapp.controller;

import com.minierpapp.controller.base.BaseRestController;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.item.dto.ItemDto;
import com.minierpapp.model.item.dto.ItemRequest;
import com.minierpapp.model.item.dto.ItemResponse;
import com.minierpapp.model.item.mapper.ItemMapper;
import com.minierpapp.service.ItemService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController extends BaseRestController<Item, ItemDto, ItemRequest, ItemResponse> {
    private final ItemService itemService;

    public ItemController(ItemMapper mapper, ItemService itemService) {
        super(mapper);
        this.itemService = itemService;
    }

    @Override
    protected List<ItemResponse> findAllEntities() {
        return itemService.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    protected ItemResponse findEntityById(Long id) {
        return mapper.toResponse(itemService.findById(id));
    }

    @Override
    protected ItemResponse createEntity(ItemRequest request) {
        return mapper.toResponse(itemService.create(request));
    }

    @Override
    protected ItemResponse updateEntity(Long id, ItemRequest request) {
        return mapper.toResponse(itemService.update(id, request));
    }

    @Override
    protected void deleteEntity(Long id) {
        itemService.delete(id);
    }

    @Override
    protected List<ItemResponse> searchEntities(String keyword) {
        return itemService.searchItems(keyword);
    }

    @GetMapping("/code/{itemCode}")
    public ResponseEntity<ItemResponse> findByCode(@PathVariable String itemCode) {
        return itemService.findByItemCode(itemCode)
                .map(item -> ResponseEntity.ok(mapper.toResponse(item)))
                .orElse(ResponseEntity.notFound().build());
    }
}