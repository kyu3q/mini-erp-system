package com.minierpapp.controller;

import com.minierpapp.model.product.dto.ProductRequest;
import com.minierpapp.model.product.dto.ProductResponse;
import com.minierpapp.model.product.mapper.ProductMapper;
import com.minierpapp.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> findAll() {
        return ResponseEntity.ok(
                productService.findAll().stream()
                        .map(productMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(
                productMapper.toResponse(productService.findById(id))
        );
    }

    @GetMapping("/code/{productCode}")
    public ResponseEntity<ProductResponse> findByProductCode(@PathVariable String productCode) {
        return ResponseEntity.ok(
                productMapper.toResponse(productService.findByProductCode(productCode))
        );
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return new ResponseEntity<>(
                productMapper.toResponse(productService.create(request)),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(
                productMapper.toResponse(productService.update(id, request))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}