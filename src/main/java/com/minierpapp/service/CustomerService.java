package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.customer.dto.CustomerRequest;
import com.minierpapp.model.customer.dto.CustomerResponse;
import com.minierpapp.model.customer.mapper.CustomerMapper;
import com.minierpapp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public List<CustomerResponse> findAll(String customerCode, String name) {
        return customerRepository.findByCustomerCodeAndName(customerCode, name)
                .stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> searchCustomers(String keyword) {
        keyword = keyword != null ? keyword.trim() : "";
        List<Customer> customers = customerRepository.findByCustomerCodeContainingOrNameContainingAndDeletedFalse(keyword, keyword);
        return customers.stream()
            .map(customerMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<Customer> findAllActive() {
        return customerRepository.findByDeletedFalse();
    }

    @Transactional(readOnly = true)
    public CustomerResponse findByCustomerCode(String customerCode) {
        return customerRepository.findByCustomerCodeAndDeletedFalse(customerCode)
                .map(customerMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "customerCode", customerCode));
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        return customerRepository.findById(id)
                .map(customerMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    @Transactional(readOnly = true)
    public boolean existsByCode(String customerCode) {
        return customerRepository.existsByCustomerCodeAndDeletedFalse(customerCode);
    }

    @Transactional(readOnly = true)
    public Customer findByCode(String customerCode) {
        return customerRepository.findByCustomerCodeAndDeletedFalse(customerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "customerCode", customerCode));
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByCustomerCodeAndDeletedFalse(request.getCustomerCode())) {
            throw new IllegalArgumentException("得意先コード " + request.getCustomerCode() + " は既に使用されています");
        }

        Customer customer = customerMapper.toEntity(request);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        customerRepository.findByCustomerCodeAndDeletedFalse(request.getCustomerCode())
                .ifPresent(existingCustomer -> {
                    if (!existingCustomer.getId().equals(id)) {
                        throw new IllegalArgumentException("得意先コード " + request.getCustomerCode() + " は既に使用されています");
                    }
                });

        customerMapper.updateEntity(request, customer);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        customer.setDeleted(true);
        customerRepository.save(customer);
    }
}