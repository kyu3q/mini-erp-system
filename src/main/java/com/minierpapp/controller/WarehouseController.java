package com.minierpapp.controller;

import com.minierpapp.model.warehouse.dto.WarehouseDto;
import com.minierpapp.model.warehouse.dto.WarehouseRequest;
import com.minierpapp.model.warehouse.dto.WarehouseResponse;
import com.minierpapp.model.warehouse.mapper.WarehouseMapper;
import com.minierpapp.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final WarehouseMapper warehouseMapper;

    @GetMapping
    public ResponseEntity<Page<WarehouseResponse>> findAll(Pageable pageable) {
        Page<WarehouseResponse> response = warehouseService.findAll(pageable)
                .map(warehouseMapper::toResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarehouseResponse> findById(@PathVariable Long id) {
        WarehouseDto dto = warehouseService.findById(id);
        return ResponseEntity.ok(warehouseMapper.toResponse(dto));
    }

    @GetMapping("/code/{warehouseCode}")
    public ResponseEntity<WarehouseResponse> findByWarehouseCode(@PathVariable String warehouseCode) {
        WarehouseDto dto = warehouseService.findByWarehouseCode(warehouseCode);
        return ResponseEntity.ok(warehouseMapper.toResponse(dto));
    }

    @PostMapping
    public ResponseEntity<WarehouseResponse> create(@Valid @RequestBody WarehouseRequest request) {
        WarehouseDto dto = warehouseService.create(request);
        return ResponseEntity.ok(warehouseMapper.toResponse(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WarehouseResponse> update(@PathVariable Long id, @Valid @RequestBody WarehouseRequest request) {
        WarehouseDto dto = warehouseService.update(id, request);
        return ResponseEntity.ok(warehouseMapper.toResponse(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        warehouseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}