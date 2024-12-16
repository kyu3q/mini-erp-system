package com.minierpapp.controller;

import com.minierpapp.controller.base.BaseWebController;
import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.customer.dto.CustomerDto;
import com.minierpapp.model.customer.dto.CustomerRequest;
import com.minierpapp.model.customer.dto.CustomerResponse;
import com.minierpapp.model.customer.mapper.CustomerMapper;
import com.minierpapp.service.CustomerService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/customers")
public class CustomerWebController extends BaseWebController<Customer, CustomerDto, CustomerRequest, CustomerResponse> {

    private final CustomerService customerService;

    public CustomerWebController(CustomerMapper mapper, MessageSource messageSource, CustomerService customerService) {
        super(mapper, messageSource, "customer", "Customer");
        this.customerService = customerService;
    }

    @Override
    @GetMapping
    public String list(
            @RequestParam(required = false) String searchParam1,
            @RequestParam(required = false) String searchParam2,
            Model model) {
        String customerCode = searchParam1;
        String name = searchParam2;
        model.addAttribute("customers", customerService.findAll(customerCode, name));
        model.addAttribute("customerCode", customerCode);
        model.addAttribute("name", name);
        return getListTemplate();
    }

    @Override
    protected List<CustomerResponse> findAll() {
        return customerService.findAll(null, null);
    }

    @Override
    protected CustomerRequest createNewRequest() {
        return new CustomerRequest();
    }

    @Override
    protected CustomerRequest findById(Long id) {
        CustomerResponse customerResponse = customerService.findById(id);
        CustomerRequest request = new CustomerRequest();
        request.setCustomerCode(customerResponse.getCustomerCode());
        request.setName(customerResponse.getName());
        request.setNameKana(customerResponse.getNameKana());
        request.setPostalCode(customerResponse.getPostalCode());
        request.setAddress(customerResponse.getAddress());
        request.setPhone(customerResponse.getPhone());
        request.setEmail(customerResponse.getEmail());
        request.setFax(customerResponse.getFax());
        request.setContactPerson(customerResponse.getContactPerson());
        request.setPaymentTerms(customerResponse.getPaymentTerms());
        request.setStatus(customerResponse.getStatus());
        request.setNotes(customerResponse.getNotes());
        return request;
    }

    @Override
    protected void createEntity(CustomerRequest request) {
        customerService.create(request);
    }

    @Override
    protected void updateEntity(Long id, CustomerRequest request) {
        customerService.update(id, request);
    }

    @Override
    protected void deleteEntity(Long id) {
        customerService.delete(id);
    }

    @Override
    protected void handleError(BindingResult result, Exception e) {
        if (e.getMessage().contains("得意先コード")) {
            result.rejectValue("customerCode", "error.customerCode", e.getMessage());
        } else {
            super.handleError(result, e);
        }
    }
}