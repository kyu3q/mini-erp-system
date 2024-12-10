package com.minierpapp.controller;

import com.minierpapp.model.item.dto.ItemRequest;
import com.minierpapp.model.item.dto.ItemResponse;
import com.minierpapp.model.item.mapper.ItemMapper;
import com.minierpapp.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final ItemMapper itemMapper;

    @GetMapping
    public ResponseEntity<List<ItemResponse>> findAll() {
        return ResponseEntity.ok(
                itemService.findAll().stream()
                        .map(itemMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(
                itemMapper.toResponse(itemService.findById(id))
        );
    }

    @GetMapping("/code/{itemCode}")
    public ResponseEntity<ItemResponse> findByItemCode(@PathVariable String itemCode) {
        return ResponseEntity.ok(
                itemMapper.toResponse(itemService.findByItemCode(itemCode))
        );
    }

    @PostMapping
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody ItemRequest request) {
        return new ResponseEntity<>(
                itemMapper.toResponse(itemService.create(request)),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequest request) {
        return ResponseEntity.ok(
                itemMapper.toResponse(itemService.update(id, request))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}