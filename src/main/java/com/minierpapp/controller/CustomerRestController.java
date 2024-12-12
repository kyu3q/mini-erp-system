package com.minierpapp.controller;

import com.minierpapp.model.customer.dto.CustomerResponse;
import com.minierpapp.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerRestController {
    private final CustomerService customerService;

    @GetMapping("/search")
    public List<CustomerResponse> searchCustomers(@RequestParam String keyword) {
        return customerService.searchCustomers(keyword);
    }
}