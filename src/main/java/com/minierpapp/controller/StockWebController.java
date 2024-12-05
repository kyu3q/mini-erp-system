package com.minierpapp.controller;

import com.minierpapp.service.ProductService;
import com.minierpapp.service.StockService;
import com.minierpapp.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StockWebController {

    private final StockService stockService;
    private final WarehouseService warehouseService;
    private final ProductService productService;

    @GetMapping
    public String list(@PageableDefault(size = 10) Pageable pageable, Model model) {
        model.addAttribute("stocks", stockService.findAll(pageable));
        model.addAttribute("warehouses", warehouseService.findAll(pageable));
        model.addAttribute("products", productService.findAll());
        return "stock/list";
    }
}