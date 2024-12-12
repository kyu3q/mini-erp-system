package com.minierpapp.controller;

import com.minierpapp.model.price.dto.PriceRequest;
import com.minierpapp.model.price.dto.PriceResponse;
import com.minierpapp.service.PriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
public class PriceWebController {
    private final PriceService priceService;

    @PostMapping
    public ResponseEntity<PriceResponse> createPrice(@Valid @RequestBody PriceRequest request) {
        return ResponseEntity.ok(priceService.createPrice(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PriceResponse> updatePrice(
            @PathVariable Long id,
            @Valid @RequestBody PriceRequest request) {
        return ResponseEntity.ok(priceService.updatePrice(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PriceResponse> getPrice(@PathVariable Long id) {
        return ResponseEntity.ok(priceService.getPrice(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrice(@PathVariable Long id) {
        priceService.deletePrice(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sales")
    public ResponseEntity<BigDecimal> getSalesPrice(
            @RequestParam String itemCode,
            @RequestParam(required = false) String customerCode,
            @RequestParam BigDecimal quantity,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate priceDate) {
        return ResponseEntity.ok(priceService.getSalesPrice(itemCode, customerCode, quantity, priceDate));
    }

    @GetMapping("/purchase")
    public ResponseEntity<BigDecimal> getPurchasePrice(
            @RequestParam String itemCode,
            @RequestParam String supplierCode,
            @RequestParam(required = false) String customerCode,
            @RequestParam BigDecimal quantity,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate priceDate) {
        return ResponseEntity.ok(priceService.getPurchasePrice(itemCode, supplierCode, customerCode, quantity, priceDate));
    }
}