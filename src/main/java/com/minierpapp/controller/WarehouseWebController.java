package com.minierpapp.controller;

import com.minierpapp.model.warehouse.dto.WarehouseDto;
import com.minierpapp.model.warehouse.dto.WarehouseRequest;
import com.minierpapp.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/new")
    public String newWarehouse(Model model) {
        model.addAttribute("warehouse", new WarehouseRequest());
        return "warehouse/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("warehouse") WarehouseRequest request,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "warehouse/form";
        }

        try {
            warehouseService.create(request);
            redirectAttributes.addFlashAttribute("message", "倉庫を登録しました。");
            return "redirect:/warehouses";
        } catch (IllegalArgumentException e) {
            result.rejectValue("warehouseCode", "error.warehouseCode", e.getMessage());
            return "warehouse/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        WarehouseDto warehouse = warehouseService.findById(id);
        WarehouseRequest request = new WarehouseRequest();
        request.setWarehouseCode(warehouse.getWarehouseCode());
        request.setName(warehouse.getName());
        request.setAddress(warehouse.getAddress());
        request.setCapacity(warehouse.getCapacity());
        request.setStatus(warehouse.getStatus());
        request.setDescription(warehouse.getDescription());
        model.addAttribute("warehouse", request);
        return "warehouse/form";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute("warehouse") WarehouseRequest request,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "warehouse/form";
        }

        try {
            warehouseService.update(id, request);
            redirectAttributes.addFlashAttribute("message", "倉庫を更新しました。");
            return "redirect:/warehouses";
        } catch (IllegalArgumentException e) {
            result.rejectValue("warehouseCode", "error.warehouseCode", e.getMessage());
            return "warehouse/form";
        }
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        warehouseService.delete(id);
        redirectAttributes.addFlashAttribute("message", "倉庫を削除しました。");
        return "redirect:/warehouses";
    }
}