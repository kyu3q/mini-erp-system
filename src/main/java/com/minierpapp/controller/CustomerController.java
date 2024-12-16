package com.minierpapp.controller;

import com.minierpapp.controller.base.BaseRestController;
import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.customer.dto.CustomerDto;
import com.minierpapp.model.customer.dto.CustomerRequest;
import com.minierpapp.model.customer.dto.CustomerResponse;
import com.minierpapp.model.customer.mapper.CustomerMapper;
import com.minierpapp.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController extends BaseRestController<Customer, CustomerDto, CustomerRequest, CustomerResponse> {

    private final CustomerService customerService;

    public CustomerController(CustomerMapper mapper, CustomerService customerService) {
        super(mapper);
        this.customerService = customerService;
    }

    @GetMapping("/code/{customerCode}")
    public ResponseEntity<CustomerResponse> findByCustomerCode(@PathVariable String customerCode) {
        return ResponseEntity.ok(customerService.findByCustomerCode(customerCode));
    }

    @Override
    protected List<CustomerResponse> findAllEntities() {
        return customerService.findAll(null, null);
    }

    @Override
    protected CustomerResponse findEntityById(Long id) {
        return customerService.findById(id);
    }

    @Override
    protected CustomerResponse createEntity(CustomerRequest request) {
        return customerService.create(request);
    }

    @Override
    protected CustomerResponse updateEntity(Long id, CustomerRequest request) {
        return customerService.update(id, request);
    }

    @Override
    protected void deleteEntity(Long id) {
        customerService.delete(id);
    }

    @Override
    protected List<CustomerResponse> searchEntities(String keyword) {
        return customerService.searchCustomers(keyword);
    }
}