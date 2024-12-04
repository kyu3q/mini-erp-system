package com.minierpapp.controller;

import com.minierpapp.model.product.Product;
import com.minierpapp.model.product.dto.ProductRequest;
import com.minierpapp.model.product.mapper.ProductMapper;
import com.minierpapp.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductWebController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.findAll());
        return "products/list";
    }

    @GetMapping("/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new ProductRequest());
        return "products/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("product") ProductRequest request,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "products/form";
        }

        try {
            productService.create(request);
            redirectAttributes.addFlashAttribute("message", "商品を登録しました。");
            return "redirect:/products";
        } catch (IllegalArgumentException e) {
            result.rejectValue("productCode", "error.productCode", e.getMessage());
            return "products/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        ProductRequest request = new ProductRequest();
        request.setProductCode(product.getProductCode());
        request.setProductName(product.getProductName());
        request.setDescription(product.getDescription());
        request.setUnit(product.getUnit());
        request.setStatus(product.getStatus());
        request.setMinimumStock(product.getMinimumStock());
        request.setMaximumStock(product.getMaximumStock());
        request.setReorderPoint(product.getReorderPoint());
        model.addAttribute("product", request);
        return "products/form";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute("product") ProductRequest request,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "products/form";
        }

        try {
            productService.update(id, request);
            redirectAttributes.addFlashAttribute("message", "商品を更新しました。");
            return "redirect:/products";
        } catch (IllegalArgumentException e) {
            result.rejectValue("productCode", "error.productCode", e.getMessage());
            return "products/form";
        }
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("message", "商品を削除しました。");
        return "redirect:/products";
    }
}