package com.minierpapp.controller;

import com.minierpapp.model.price.PriceType;
import com.minierpapp.model.price.dto.PriceConditionRequest;
import com.minierpapp.service.CustomerService;
import com.minierpapp.service.ItemService;
import com.minierpapp.service.PriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;

@Controller
@RequestMapping("/prices/sales")
@RequiredArgsConstructor
public class SalesPriceController {
    private final PriceService priceService;
    private final ItemService itemService;
    private final CustomerService customerService;

    @GetMapping
    public String list(
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) Long customerId,
            Model model) {
        model.addAttribute("prices", priceService.findSalesPrices(itemId, customerId));
        model.addAttribute("items", itemService.findAllActive());
        model.addAttribute("customers", customerService.findAllActive());
        model.addAttribute("selectedItemId", itemId);
        model.addAttribute("selectedCustomerId", customerId);
        return "price/sales/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        var request = new PriceConditionRequest();
        request.setPriceType(PriceType.SALES);
        request.setPriceScales(new ArrayList<>());
        model.addAttribute("priceRequest", request);
        return "price/sales/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        var price = priceService.getPrice(id);
        model.addAttribute("priceRequest", price);
        if (price.getItemId() != null) {
            model.addAttribute("item", itemService.findById(price.getItemId()));
        }
        if (price.getCustomerId() != null) {
            model.addAttribute("customer", customerService.findById(price.getCustomerId()));
        }
        return "price/sales/form";
    }

    @PostMapping
    public String save(
            @Valid @ModelAttribute("priceRequest") PriceConditionRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            if (request.getItemId() != null) {
                model.addAttribute("item", itemService.findById(request.getItemId()));
            }
            if (request.getCustomerId() != null) {
                model.addAttribute("customer", customerService.findById(request.getCustomerId()));
            }
            return "price/sales/form";
        }

        try {
            if (request.getId() == null) {
                priceService.createPrice(request);
                redirectAttributes.addFlashAttribute("message", "販売単価を登録しました。");
                redirectAttributes.addFlashAttribute("messageType", "success");
            } else {
                priceService.updatePrice(request.getId(), request);
                redirectAttributes.addFlashAttribute("message", "販売単価を更新しました。");
                redirectAttributes.addFlashAttribute("messageType", "success");
            }
        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
            model.addAttribute("messageType", "danger");
            if (request.getItemId() != null) {
                model.addAttribute("item", itemService.findById(request.getItemId()));
            }
            if (request.getCustomerId() != null) {
                model.addAttribute("customer", customerService.findById(request.getCustomerId()));
            }
            return "price/sales/form";
        }

        return "redirect:/prices/sales";
    }

    @DeleteMapping("/{id}")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            priceService.deletePrice(id);
            redirectAttributes.addFlashAttribute("successMessage", "販売単価を削除しました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/prices/sales";
    }
}