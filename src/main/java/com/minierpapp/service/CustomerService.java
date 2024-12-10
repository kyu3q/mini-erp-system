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

    public List<CustomerDto> findAll(String customerCode, String name) {
        return customerRepository.findByCustomerCodeAndName(customerCode, name)
                .stream()
                .map(CustomerMapper.INSTANCE::toDto)
                .collect(Collectors.toList());
    }

    public CustomerDto findById(Long id) {
        return customerRepository.findById(id)
                .map(CustomerMapper.INSTANCE::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    @Transactional
    public CustomerDto create(CustomerRequest request) {
        if (customerRepository.existsByCustomerCodeAndDeletedFalse(request.getCustomerCode())) {
            throw new IllegalArgumentException("得意先コード " + request.getCustomerCode() + " は既に使用されています");
        }

        Customer customer = CustomerMapper.INSTANCE.toEntity(request);
        return CustomerMapper.INSTANCE.toDto(customerRepository.save(customer));
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

        CustomerMapper.INSTANCE.updateEntity(request, customer);
        return CustomerMapper.INSTANCE.toDto(customerRepository.save(customer));
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        customer.setDeleted(true);
        customerRepository.save(customer);
    }
}