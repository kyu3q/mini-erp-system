package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.customer.dto.CustomerRequest;
import com.minierpapp.model.customer.dto.CustomerResponse;
import com.minierpapp.model.customer.mapper.CustomerMapper;
import com.minierpapp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
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
        Customer customer = customerRepository.findByCustomerCodeAndDeletedFalse(customerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "customerCode", customerCode));

        if (!hasAccessToCustomer(customer)) {
            throw new AccessDeniedException("Access denied");
        }

        return customerMapper.toResponse(customer);
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
        if (request.getCustomerCode() == null || request.getCustomerCode().trim().isEmpty()) {
            throw new IllegalArgumentException("得意先コードを入力してください");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("得意先名は必須です");
        }

        if (customerRepository.existsByCustomerCodeAndDeletedFalse(request.getCustomerCode())) {
            throw new IllegalArgumentException("得意先コード " + request.getCustomerCode() + " は既に使用されています");
        }

        Customer customer = customerMapper.toEntity(request);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        if (request.getCustomerCode() == null || request.getCustomerCode().trim().isEmpty()) {
            throw new IllegalArgumentException("得意先コードを入力してください");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("得意先名は必須です");
        }

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

    @Transactional(readOnly = true)
    public Page<CustomerResponse> search(String keyword, Pageable pageable) {
        Page<Customer> customerPage = customerRepository.findByNameContainingAndDeletedFalse(keyword, pageable);
        return customerPage.map(customerMapper::toResponse);
    }

    @Transactional
    public List<CustomerResponse> bulkCreate(List<CustomerRequest> requests) {
        return requests.stream()
            .map(request -> {
                if (request.getCustomerCode() == null || request.getCustomerCode().trim().isEmpty()) {
                    throw new IllegalArgumentException("得意先コードを入力してください");
                }
                if (request.getName() == null || request.getName().trim().isEmpty()) {
                    throw new IllegalArgumentException("得意先名は必須です");
                }
                if (customerRepository.existsByCustomerCodeAndDeletedFalse(request.getCustomerCode())) {
                    throw new IllegalArgumentException("得意先コード " + request.getCustomerCode() + " は既に使用されています");
                }
                Customer customer = customerMapper.toEntity(request);
                return customerMapper.toResponse(customerRepository.save(customer));
            })
            .collect(Collectors.toList());
    }

    public boolean hasAccessToCustomer(Customer customer) {
        // TODO: 実際のセキュリティロジックを実装
        // 例: 現在のユーザーが顧客にアクセスする権限があるかどうかをチェック
        return true;
    }
}