package com.minierpapp.controller;

import com.minierpapp.model.warehouse.dto.WarehouseDto;
import com.minierpapp.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/warehouses")
@RequiredArgsConstructor
public class WarehouseWebController {

    private final WarehouseService warehouseService;

    @GetMapping
    public String list(@PageableDefault Pageable pageable, Model model) {
        Page<WarehouseDto> warehouses = warehouseService.findAll(pageable);
        model.addAttribute("warehouses", warehouses);
        return "warehouse/list";
    }
}