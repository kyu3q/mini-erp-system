package com.minierpapp.service;

import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.common.Status;
import com.minierpapp.model.customer.dto.CustomerRequest;
import com.minierpapp.model.customer.dto.CustomerResponse;
import com.minierpapp.model.customer.mapper.CustomerMapper;
import com.minierpapp.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;
    private CustomerResponse testCustomerResponse;
    private CustomerRequest testCustomerRequest;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setCustomerCode("CUST001");
        testCustomer.setName("Test Customer");
        testCustomer.setStatus(Status.ACTIVE);
        testCustomer.setCreatedAt(LocalDateTime.now());

        testCustomerResponse = new CustomerResponse();
        testCustomerResponse.setId(1L);
        testCustomerResponse.setCustomerCode("CUST001");
        testCustomerResponse.setName("Test Customer");
        testCustomerResponse.setStatus(Status.ACTIVE);

        testCustomerRequest = new CustomerRequest();
        testCustomerRequest.setCustomerCode("CUST001");
        testCustomerRequest.setName("Test Customer");
        testCustomerRequest.setStatus(Status.ACTIVE);
    }

    @Test
    void whenGetCustomerByCode_thenReturnCustomer() {
        // given
        given(customerRepository.findByCustomerCodeAndDeletedFalse("CUST001"))
            .willReturn(Optional.of(testCustomer));
        given(customerMapper.toResponse(testCustomer))
            .willReturn(testCustomerResponse);

        // when
        CustomerResponse found = customerService.findByCustomerCode("CUST001");

        // then
        assertThat(found).isNotNull();
        assertThat(found.getCustomerCode()).isEqualTo("CUST001");
        verify(customerRepository).findByCustomerCodeAndDeletedFalse("CUST001");
    }

    @Test
    void whenCreateCustomer_thenReturnSavedCustomer() {
        // given
        given(customerMapper.toEntity(testCustomerRequest))
            .willReturn(testCustomer);
        given(customerRepository.save(testCustomer))
            .willReturn(testCustomer);
        given(customerMapper.toResponse(testCustomer))
            .willReturn(testCustomerResponse);

        // when
        CustomerResponse saved = customerService.create(testCustomerRequest);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getCustomerCode()).isEqualTo("CUST001");
        verify(customerRepository).save(testCustomer);
    }

    @Test
    void whenDeleteCustomer_thenCustomerIsDeleted() {
        // given
        given(customerRepository.findById(1L))
            .willReturn(Optional.of(testCustomer));

        // when
        customerService.delete(1L);

        // then
        verify(customerRepository).findById(1L);
        verify(customerRepository).save(testCustomer);
        assertThat(testCustomer.isDeleted()).isTrue();
    }
}