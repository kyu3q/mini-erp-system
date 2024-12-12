package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.customer.dto.CustomerDto;
import com.minierpapp.model.customer.dto.CustomerRequest;
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

    public List<CustomerDto> findAll(String customerCode, String name) {
        return customerRepository.findByCustomerCodeAndName(customerCode, name)
                .stream()
                .map(customerMapper::toDto)
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

    public CustomerDto findById(Long id) {
        return customerRepository.findById(id)
                .map(customerMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    @Transactional
    public CustomerDto create(CustomerRequest request) {
        if (customerRepository.existsByCustomerCodeAndDeletedFalse(request.getCustomerCode())) {
            throw new IllegalArgumentException("得意先コード " + request.getCustomerCode() + " は既に使用されています");
        }

        Customer customer = customerMapper.toEntity(request);
        return customerMapper.toDto(customerRepository.save(customer));
    }

    @Transactional
    public CustomerDto update(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        customerRepository.findByCustomerCodeAndDeletedFalse(request.getCustomerCode())
                .ifPresent(existingCustomer -> {
                    if (!existingCustomer.getId().equals(id)) {
                        throw new IllegalArgumentException("得意先コード " + request.getCustomerCode() + " は既に使用されています");
                    }
                });

        customerMapper.updateEntity(request, customer);
        return customerMapper.toDto(customerRepository.save(customer));
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        customer.setDeleted(true);
        customerRepository.save(customer);
    }
}