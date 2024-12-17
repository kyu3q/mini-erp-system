package com.minierpapp.controller;

import com.minierpapp.model.price.PriceType;
import com.minierpapp.model.price.dto.PriceConditionRequest;
import com.minierpapp.service.CustomerService;
import com.minierpapp.service.ItemService;
import com.minierpapp.service.PriceService;
import com.minierpapp.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;

@Controller
@RequestMapping("/prices/purchase")
@RequiredArgsConstructor
public class PurchasePriceController {
    private final PriceService priceService;
    private final ItemService itemService;
    private final SupplierService supplierService;
    private final CustomerService customerService;

    @GetMapping
    public String list(
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long customerId,
            Model model) {
        model.addAttribute("prices", priceService.findPurchasePrices(itemId, supplierId, customerId));
        model.addAttribute("items", itemService.findAllActive());
        model.addAttribute("suppliers", supplierService.findAllActive());
        model.addAttribute("customers", customerService.findAllActive());
        model.addAttribute("selectedItemId", itemId);
        model.addAttribute("selectedSupplierId", supplierId);
        model.addAttribute("selectedCustomerId", customerId);
        return "price/purchase/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        var request = new PriceConditionRequest();
        request.setPriceType(PriceType.PURCHASE);
        request.setPriceScales(new ArrayList<>());
        model.addAttribute("priceRequest", request);
        return "price/purchase/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        var price = priceService.getPrice(id);
        model.addAttribute("priceRequest", price);
        if (price.getItemId() != null) {
            model.addAttribute("item", itemService.findById(price.getItemId()));
        }
        if (price.getSupplierId() != null) {
            model.addAttribute("supplier", supplierService.findById(price.getSupplierId()));
        }
        if (price.getCustomerId() != null) {
            model.addAttribute("customer", customerService.findById(price.getCustomerId()));
        }
        return "price/purchase/form";
    }

    @PostMapping
    public String save(
            @Valid @ModelAttribute("priceRequest") PriceConditionRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        // コードの存在チェックとIDの設定
        if (request.getItemCode() != null && !request.getItemCode().isEmpty()) {
            try {
                var item = itemService.findByItemCode(request.getItemCode());
                request.setItemId(item.getId());
            } catch (Exception e) {
                result.rejectValue("itemCode", "error.itemCode", 
                    String.format("品目コード '%s' は存在しません。", request.getItemCode()));
            }
        }

        if (request.getSupplierCode() != null && !request.getSupplierCode().isEmpty()) {
            try {
                var supplier = supplierService.findBySupplierCode(request.getSupplierCode());
                request.setSupplierId(supplier.getId());
            } catch (Exception e) {
                result.rejectValue("supplierCode", "error.supplierCode", 
                    String.format("仕入先コード '%s' は存在しません。", request.getSupplierCode()));
            }
        }

        if (request.getCustomerCode() != null && !request.getCustomerCode().isEmpty()) {
            try {
                var customer = customerService.findByCustomerCode(request.getCustomerCode());
                request.setCustomerId(customer.getId());
            } catch (Exception e) {
                result.rejectValue("customerCode", "error.customerCode", 
                    String.format("得意先コード '%s' は存在しません。", request.getCustomerCode()));
            }
        }

        // 必須項目のチェック
        if (request.getItemId() == null) {
            result.rejectValue("itemCode", "error.required", "品目は必須です。");
        }

        if (request.getSupplierId() == null) {
            result.rejectValue("supplierCode", "error.required", "仕入先は必須です。");
        }

        if (result.hasErrors()) {
            // エラー時に既存の選択値を保持
            if (request.getItemId() != null) {
                model.addAttribute("item", itemService.findById(request.getItemId()));
            }
            if (request.getSupplierId() != null) {
                model.addAttribute("supplier", supplierService.findById(request.getSupplierId()));
            }
            if (request.getCustomerId() != null) {
                model.addAttribute("customer", customerService.findById(request.getCustomerId()));
            }
            return "price/purchase/form";
        }

        try {
            if (request.getId() == null) {
                priceService.createPrice(request);
                redirectAttributes.addFlashAttribute("message", "購買単価を登録しました。");
                redirectAttributes.addFlashAttribute("messageType", "success");
            } else {
                priceService.updatePrice(request.getId(), request);
                redirectAttributes.addFlashAttribute("message", "購買単価を更新しました。");
                redirectAttributes.addFlashAttribute("messageType", "success");
            }
        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
            model.addAttribute("messageType", "danger");
            if (request.getItemId() != null) {
                model.addAttribute("item", itemService.findById(request.getItemId()));
            }
            if (request.getSupplierId() != null) {
                model.addAttribute("supplier", supplierService.findById(request.getSupplierId()));
            }
            if (request.getCustomerId() != null) {
                model.addAttribute("customer", customerService.findById(request.getCustomerId()));
            }
            return "price/purchase/form";
        }

        return "redirect:/prices/purchase";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            priceService.deletePrice(id);
            redirectAttributes.addFlashAttribute("message", "購買単価を削除しました。");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/prices/purchase";
    }
}