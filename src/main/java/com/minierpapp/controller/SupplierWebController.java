package com.minierpapp.controller;

import com.minierpapp.model.supplier.dto.SupplierDto;
import com.minierpapp.model.supplier.dto.SupplierRequest;
import com.minierpapp.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierWebController {
    private final SupplierService supplierService;

    @GetMapping
    public String list(@RequestParam(required = false) String supplierCode,
                      @RequestParam(required = false) String name,
                      Model model) {
        model.addAttribute("suppliers", supplierService.findAll(supplierCode, name));
        return "supplier/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("supplierRequest", new SupplierRequest());
        return "supplier/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute SupplierRequest supplierRequest,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "supplier/form";
        }

        try {
            supplierService.create(supplierRequest);
            redirectAttributes.addFlashAttribute("message", "仕入先を登録しました。");
            return "redirect:/suppliers";
        } catch (IllegalArgumentException e) {
            result.rejectValue("supplierCode", "error.supplierCode", e.getMessage());
            return "supplier/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        SupplierDto supplierDto = supplierService.findById(id);
        SupplierRequest supplierRequest = new SupplierRequest();
        // DTOの値をRequestにコピー
        supplierRequest.setSupplierCode(supplierDto.getSupplierCode());
        supplierRequest.setName(supplierDto.getName());
        supplierRequest.setNameKana(supplierDto.getNameKana());
        supplierRequest.setPostalCode(supplierDto.getPostalCode());
        supplierRequest.setAddress(supplierDto.getAddress());
        supplierRequest.setPhone(supplierDto.getPhone());
        supplierRequest.setEmail(supplierDto.getEmail());
        supplierRequest.setFax(supplierDto.getFax());
        supplierRequest.setContactPerson(supplierDto.getContactPerson());
        supplierRequest.setPaymentTerms(supplierDto.getPaymentTerms());
        supplierRequest.setStatus(supplierDto.getStatus());
        supplierRequest.setNotes(supplierDto.getNotes());

        model.addAttribute("supplierRequest", supplierRequest);
        model.addAttribute("supplierId", id);
        return "supplier/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute SupplierRequest supplierRequest,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "supplier/form";
        }

        try {
            supplierService.update(id, supplierRequest);
            redirectAttributes.addFlashAttribute("message", "仕入先を更新しました。");
            return "redirect:/suppliers";
        } catch (IllegalArgumentException e) {
            result.rejectValue("supplierCode", "error.supplierCode", e.getMessage());
            return "supplier/form";
        }
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        supplierService.delete(id);
        redirectAttributes.addFlashAttribute("message", "仕入先を削除しました。");
        return "redirect:/suppliers";
    }
}