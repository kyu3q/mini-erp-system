package com.minierpapp.controller;

import com.minierpapp.model.stock.dto.StockDto;
import com.minierpapp.model.stock.dto.StockRequest;
import com.minierpapp.service.ProductService;
import com.minierpapp.service.StockService;
import com.minierpapp.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        model.addAttribute("warehouses", warehouseService.findAll());
        model.addAttribute("products", productService.findAll());
        return "stock/list";
    }

    @GetMapping("/new")
    public String newStock(Model model) {
        model.addAttribute("stock", new StockRequest());
        model.addAttribute("warehouses", warehouseService.findAll());
        model.addAttribute("products", productService.findAll());
        return "stock/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("stock") StockRequest request,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("warehouses", warehouseService.findAll());
            model.addAttribute("products", productService.findAll());
            return "stock/form";
        }

        try {
            stockService.create(request);
            redirectAttributes.addFlashAttribute("message", "在庫を登録しました。");
            return "redirect:/stocks";
        } catch (IllegalArgumentException e) {
            result.rejectValue("", "error.stock", e.getMessage());
            model.addAttribute("warehouses", warehouseService.findAll());
            model.addAttribute("products", productService.findAll());
            return "stock/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        StockDto stock = stockService.findById(id);
        StockRequest request = new StockRequest();
        request.setId(id);
        request.setWarehouseId(stock.getWarehouseId());
        request.setProductId(stock.getProductId());
        request.setQuantity(stock.getQuantity());
        request.setMinimumQuantity(stock.getMinimumQuantity());
        request.setMaximumQuantity(stock.getMaximumQuantity());
        request.setLocation(stock.getLocation());
        request.setNotes(stock.getNotes());
        
        model.addAttribute("stock", request);
        model.addAttribute("warehouses", warehouseService.findAll());
        model.addAttribute("products", productService.findAll());
        return "stock/form";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute("stock") StockRequest request,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("warehouses", warehouseService.findAll());
            model.addAttribute("products", productService.findAll());
            return "stock/form";
        }

        try {
            stockService.update(id, request);
            redirectAttributes.addFlashAttribute("message", "在庫を更新しました。");
            return "redirect:/stocks";
        } catch (IllegalArgumentException e) {
            result.rejectValue("", "error.stock", e.getMessage());
            model.addAttribute("warehouses", warehouseService.findAll());
            model.addAttribute("products", productService.findAll());
            return "stock/form";
        }
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        stockService.delete(id);
        redirectAttributes.addFlashAttribute("message", "在庫を削除しました。");
        return "redirect:/stocks";
    }
}