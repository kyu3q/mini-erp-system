package com.minierpapp.controller;

import com.minierpapp.model.price.Price;
import com.minierpapp.model.price.dto.PriceDetailsResponse;
import com.minierpapp.service.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
public class PriceApiController {
    private final PriceService priceService;

    @GetMapping("/{id}/details")
    public ResponseEntity<PriceDetailsResponse> getPriceDetails(@PathVariable Long id) {
        Price price = priceService.getPriceEntity(id);
        return ResponseEntity.ok(PriceDetailsResponse.from(price));
    }
}