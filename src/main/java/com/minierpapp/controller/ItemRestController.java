package com.minierpapp.controller;

import com.minierpapp.model.item.dto.ItemResponse;
import com.minierpapp.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemRestController {
    private final ItemService itemService;

    @GetMapping("/search")
    public List<ItemResponse> searchItems(@RequestParam String keyword) {
        return itemService.searchItems(keyword);
    }
}