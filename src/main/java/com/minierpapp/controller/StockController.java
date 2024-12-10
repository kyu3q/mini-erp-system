package com.minierpapp.controller;

import com.minierpapp.model.stock.dto.StockDto;
import com.minierpapp.model.stock.dto.StockRequest;
import com.minierpapp.model.stock.dto.StockResponse;
import com.minierpapp.model.stock.mapper.StockMapper;
import com.minierpapp.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final StockMapper stockMapper;

    @GetMapping
    public ResponseEntity<Page<StockResponse>> findAll(Pageable pageable) {
        Page<StockResponse> response = stockService.findAll(pageable)
                .map(stockMapper::toResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<Page<StockResponse>> findByWarehouse(
            @PathVariable Long warehouseId,
            Pageable pageable) {
        Page<StockResponse> response = stockService.findByWarehouse(warehouseId, pageable)
                .map(stockMapper::toResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<Page<StockResponse>> findByItem(
            @PathVariable Long itemId,
            Pageable pageable) {
        Page<StockResponse> response = stockService.findByItem(itemId, pageable)
                .map(stockMapper::toResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockResponse> findById(@PathVariable Long id) {
        StockDto dto = stockService.findById(id);
        return ResponseEntity.ok(stockMapper.toResponse(dto));
    }

    @PostMapping
    public ResponseEntity<StockResponse> create(@Valid @RequestBody StockRequest request) {
        StockDto dto = stockService.create(request);
        return ResponseEntity.ok(stockMapper.toResponse(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StockResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody StockRequest request) {
        StockDto dto = stockService.update(id, request);
        return ResponseEntity.ok(stockMapper.toResponse(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stockService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/adjust")
    public ResponseEntity<StockResponse> adjustQuantity(
            @PathVariable Long id,
            @RequestParam int adjustment) {
        StockDto dto = stockService.adjustQuantity(id, adjustment);
        return ResponseEntity.ok(stockMapper.toResponse(dto));
    }
}