package com.minierpapp.controller;

import com.minierpapp.model.customer.dto.CustomerDto;
import com.minierpapp.model.customer.dto.CustomerRequest;
import com.minierpapp.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerWebController {
    private final CustomerService customerService;

    @GetMapping
    public String list(@RequestParam(required = false) String customerCode,
                      @RequestParam(required = false) String name,
                      Model model) {
        model.addAttribute("customers", customerService.findAll(customerCode, name));
        return "customer/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("customerRequest", new CustomerRequest());
        return "customer/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute CustomerRequest customerRequest,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "customer/form";
        }

        try {
            customerService.create(customerRequest);
            redirectAttributes.addFlashAttribute("message", "得意先を登録しました。");
            return "redirect:/customers";
        } catch (IllegalArgumentException e) {
            result.rejectValue("customerCode", "error.customerCode", e.getMessage());
            return "customer/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        CustomerDto customerDto = customerService.findById(id);
        CustomerRequest customerRequest = new CustomerRequest();
        // DTOの値をRequestにコピー
        customerRequest.setCustomerCode(customerDto.getCustomerCode());
        customerRequest.setName(customerDto.getName());
        customerRequest.setNameKana(customerDto.getNameKana());
        customerRequest.setPostalCode(customerDto.getPostalCode());
        customerRequest.setAddress(customerDto.getAddress());
        customerRequest.setPhone(customerDto.getPhone());
        customerRequest.setEmail(customerDto.getEmail());
        customerRequest.setFax(customerDto.getFax());
        customerRequest.setContactPerson(customerDto.getContactPerson());
        customerRequest.setPaymentTerms(customerDto.getPaymentTerms());
        customerRequest.setStatus(customerDto.getStatus());
        customerRequest.setNotes(customerDto.getNotes());

        model.addAttribute("customerRequest", customerRequest);
        model.addAttribute("customerId", id);
        return "customer/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute CustomerRequest customerRequest,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "customer/form";
        }

        try {
            customerService.update(id, customerRequest);
            redirectAttributes.addFlashAttribute("message", "得意先を更新しました。");
            return "redirect:/customers";
        } catch (IllegalArgumentException e) {
            result.rejectValue("customerCode", "error.customerCode", e.getMessage());
            return "customer/form";
        }
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        customerService.delete(id);
        redirectAttributes.addFlashAttribute("message", "得意先を削除しました。");
        return "redirect:/customers";
    }
}