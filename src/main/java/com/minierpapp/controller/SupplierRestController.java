package com.minierpapp.controller;

import com.minierpapp.model.supplier.dto.SupplierResponse;
import com.minierpapp.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierRestController {
    private final SupplierService supplierService;

    @GetMapping("/search")
    public List<SupplierResponse> searchSuppliers(@RequestParam String keyword) {
        return supplierService.searchSuppliers(keyword);
    }
}