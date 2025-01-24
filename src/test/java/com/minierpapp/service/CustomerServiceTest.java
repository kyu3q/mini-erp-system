package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;

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

    // エラーケースのテスト
    @Test
    void whenCreateCustomerWithDuplicateCode_thenThrowException() {
        // given
        given(customerRepository.existsByCustomerCodeAndDeletedFalse("CUST001"))
            .willReturn(true);

        // when/then
        assertThatThrownBy(() -> customerService.create(testCustomerRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("は既に使用されています");
    }

    @Test
    void whenCreateCustomerWithInvalidData_thenThrowException() {
        // given
        testCustomerRequest.setCustomerCode("");

        // when/then
        assertThatThrownBy(() -> customerService.create(testCustomerRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("得意先コードを入力してください");
    }

    // 検索機能のテスト
    @Test
    void whenSearchCustomers_thenReturnPagedResults() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        List<Customer> customers = Arrays.asList(testCustomer);
        Page<Customer> customerPage = new PageImpl<>(customers, pageable, 1);
        
        given(customerRepository.findByNameContainingAndDeletedFalse("Test", pageable))
            .willReturn(customerPage);
        given(customerMapper.toResponse(testCustomer))
            .willReturn(testCustomerResponse);

        // when
        Page<CustomerResponse> result = customerService.search("Test", pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Customer");
    }

    // バッチ処理のテスト
    @Test
    void whenBulkCreateCustomers_thenAllCustomersAreSaved() {
        // given
        List<CustomerRequest> requests = Arrays.asList(testCustomerRequest);
        given(customerMapper.toEntity(testCustomerRequest))
            .willReturn(testCustomer);
        given(customerRepository.save(testCustomer))
            .willReturn(testCustomer);
        given(customerMapper.toResponse(testCustomer))
            .willReturn(testCustomerResponse);

        // when
        List<CustomerResponse> responses = customerService.bulkCreate(requests);

        // then
        assertThat(responses).hasSize(1);
        verify(customerRepository).save(testCustomer);
    }

    // セキュリティのテスト
    @Test
    void whenFindAll_withMultipleConditions_thenReturnFilteredCustomers() {
        // given
        List<Customer> customers = Arrays.asList(testCustomer);
        given(customerRepository.findByCustomerCodeAndName("CUST001", "Test"))
            .willReturn(customers);
        given(customerMapper.toResponse(testCustomer))
            .willReturn(testCustomerResponse);

        // when
        List<CustomerResponse> result = customerService.findAll("CUST001", "Test");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerCode()).isEqualTo("CUST001");
        verify(customerRepository).findByCustomerCodeAndName("CUST001", "Test");
    }

    @Test
    void whenFindAll_withNoResults_thenReturnEmptyList() {
        // given
        given(customerRepository.findByCustomerCodeAndName("NONEXISTENT", "NONEXISTENT"))
            .willReturn(Collections.emptyList());

        // when
        List<CustomerResponse> result = customerService.findAll("NONEXISTENT", "NONEXISTENT");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void whenSearchCustomers_withNullKeyword_thenSearchWithEmptyString() {
        // given
        List<Customer> customers = Arrays.asList(testCustomer);
        given(customerRepository.findByCustomerCodeContainingOrNameContainingAndDeletedFalse("", ""))
            .willReturn(customers);
        given(customerMapper.toResponse(testCustomer))
            .willReturn(testCustomerResponse);

        // when
        List<CustomerResponse> result = customerService.searchCustomers(null);

        // then
        assertThat(result).hasSize(1);
        verify(customerRepository).findByCustomerCodeContainingOrNameContainingAndDeletedFalse("", "");
    }

    @Test
    void whenFindAllActive_thenReturnOnlyActiveCustomers() {
        // given
        List<Customer> activeCustomers = Arrays.asList(testCustomer);
        given(customerRepository.findByDeletedFalse())
            .willReturn(activeCustomers);

        // when
        List<Customer> result = customerService.findAllActive();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isDeleted()).isFalse();
    }

    @Test
    void whenFindById_withNonExistentId_thenThrowException() {
        // given
        given(customerRepository.findById(999L))
            .willReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> customerService.findById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Customer not found with id: 999");
    }

    @Test
    void whenUpdate_withValidData_thenReturnUpdatedCustomer() {
        // given
        given(customerRepository.findById(1L))
            .willReturn(Optional.of(testCustomer));
        given(customerRepository.findByCustomerCodeAndDeletedFalse("CUST001"))
            .willReturn(Optional.of(testCustomer));
        given(customerRepository.save(testCustomer))
            .willReturn(testCustomer);
        given(customerMapper.toResponse(testCustomer))
            .willReturn(testCustomerResponse);

        // when
        CustomerResponse result = customerService.update(1L, testCustomerRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCustomerCode()).isEqualTo("CUST001");
        verify(customerMapper).updateEntity(testCustomerRequest, testCustomer);
    }

    @Test
    void whenUpdate_withNonExistentId_thenThrowException() {
        // given
        given(customerRepository.findById(999L))
            .willReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> customerService.update(999L, testCustomerRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Customer not found with id: 999");
    }

    @Test
    void whenUpdate_withInvalidData_thenThrowException() {
        // given
        testCustomerRequest.setCustomerCode("");

        // when/then
        assertThatThrownBy(() -> customerService.update(1L, testCustomerRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("得意先コードを入力してください");
    }

    @Test
    void whenBulkCreate_withEmptyList_thenReturnEmptyList() {
        // given
        List<CustomerRequest> emptyList = Collections.emptyList();

        // when
        List<CustomerResponse> result = customerService.bulkCreate(emptyList);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void whenBulkCreate_withInvalidData_thenThrowException() {
        // given
        CustomerRequest invalidRequest = new CustomerRequest();
        invalidRequest.setCustomerCode("");
        List<CustomerRequest> requests = Arrays.asList(invalidRequest);

        // when/then
        assertThatThrownBy(() -> customerService.bulkCreate(requests))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("得意先コードを入力してください");
    }

    // 境界値テスト
    @Test
    void whenCreateCustomer_withMaxLengthCode_thenSuccess() {
        // given
        String maxLengthCode = "C".repeat(50);
        testCustomerRequest.setCustomerCode(maxLengthCode);
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
        verify(customerRepository).save(testCustomer);
    }

    @Test
    void whenCreateCustomer_withSpecialCharactersInName_thenSuccess() {
        // given
        testCustomerRequest.setName("テスト株式会社 ★☆♪");
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
        verify(customerRepository).save(testCustomer);
    }

    // エッジケーステスト
    @Test
    void whenCreateCustomer_withFullWidthCharactersInCode_thenSuccess() {
        // given
        testCustomerRequest.setCustomerCode("テスト１２３");
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
        verify(customerRepository).save(testCustomer);
    }

    @Test
    void whenCreateCustomer_withOnlyWhitespaceInName_thenThrowException() {
        // given
        testCustomerRequest.setName("   ");

        // when/then
        assertThatThrownBy(() -> customerService.create(testCustomerRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("得意先名は必須です");
    }

    // 並行処理のテスト
    @Test
    void whenCreateCustomersConcurrently_thenHandleRaceCondition() throws InterruptedException {
        // given
        int numThreads = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numThreads);
        List<CustomerRequest> requests = IntStream.range(0, numThreads)
            .mapToObj(i -> {
                CustomerRequest request = new CustomerRequest();
                request.setCustomerCode("CUST" + i);
                request.setName("Test Customer " + i);
                request.setStatus(Status.ACTIVE);
                return request;
            })
            .collect(Collectors.toList());

        List<Future<CustomerResponse>> futures = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        // when
        for (CustomerRequest request : requests) {
            futures.add(executorService.submit(() -> {
                startLatch.await();
                try {
                    return customerService.create(request);
                } finally {
                    endLatch.countDown();
                }
            }));
        }

        startLatch.countDown();
        endLatch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        // then
        assertThat(futures).hasSize(numThreads);
        for (Future<CustomerResponse> future : futures) {
            assertThat(future.isDone()).isTrue();
        }
    }

    // パフォーマンステスト
    @Test
    void whenBulkCreateLargeData_thenSuccess() {
        // given
        int numRecords = 1000;
        List<CustomerRequest> requests = IntStream.range(0, numRecords)
            .mapToObj(i -> {
                CustomerRequest request = new CustomerRequest();
                request.setCustomerCode("BULK" + i);
                request.setName("Bulk Customer " + i);
                request.setStatus(Status.ACTIVE);
                return request;
            })
            .collect(Collectors.toList());

        given(customerMapper.toEntity(any(CustomerRequest.class)))
            .willAnswer(invocation -> {
                CustomerRequest req = invocation.getArgument(0);
                Customer customer = new Customer();
                customer.setCustomerCode(req.getCustomerCode());
                customer.setName(req.getName());
                customer.setStatus(req.getStatus());
                return customer;
            });

        given(customerRepository.save(any(Customer.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        given(customerMapper.toResponse(any(Customer.class)))
            .willAnswer(invocation -> {
                Customer customer = invocation.getArgument(0);
                CustomerResponse response = new CustomerResponse();
                response.setCustomerCode(customer.getCustomerCode());
                response.setName(customer.getName());
                response.setStatus(customer.getStatus());
                return response;
            });

        // when
        long startTime = System.currentTimeMillis();
        List<CustomerResponse> responses = customerService.bulkCreate(requests);
        long endTime = System.currentTimeMillis();

        // then
        assertThat(responses).hasSize(numRecords);
        assertThat(endTime - startTime).isLessThan(5000); // 5秒以内に完了すること
    }

    @Test
    void whenSearchLargeData_thenSuccess() {
        // given
        int numRecords = 1000;
        List<Customer> customers = IntStream.range(0, numRecords)
            .mapToObj(i -> {
                Customer customer = new Customer();
                customer.setCustomerCode("SEARCH" + i);
                customer.setName("Search Customer " + i);
                customer.setStatus(Status.ACTIVE);
                return customer;
            })
            .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(0, 50, Sort.by("name").ascending());
        Page<Customer> customerPage = new PageImpl<>(customers.subList(0, 50), pageable, numRecords);

        given(customerRepository.findByNameContainingAndDeletedFalse("Search", pageable))
            .willReturn(customerPage);

        given(customerMapper.toResponse(any(Customer.class)))
            .willAnswer(invocation -> {
                Customer customer = invocation.getArgument(0);
                CustomerResponse response = new CustomerResponse();
                response.setCustomerCode(customer.getCustomerCode());
                response.setName(customer.getName());
                response.setStatus(customer.getStatus());
                return response;
            });

        // when
        long startTime = System.currentTimeMillis();
        Page<CustomerResponse> result = customerService.search("Search", pageable);
        long endTime = System.currentTimeMillis();

        // then
        assertThat(result.getContent()).hasSize(50);
        assertThat(result.getTotalElements()).isEqualTo(numRecords);
        assertThat(endTime - startTime).isLessThan(1000); // 1秒以内に完了すること
    }

    // セキュリティのテスト
    @Test
    void whenUnauthorizedUserAccessesCustomer_thenThrowException() {
        // given
        given(customerRepository.findByCustomerCodeAndDeletedFalse("CUST001"))
            .willReturn(Optional.of(testCustomer));

        CustomerService spyService = spy(customerService);
        doReturn(false).when(spyService).hasAccessToCustomer(testCustomer);

        // when/then
        assertThatThrownBy(() -> spyService.findByCustomerCode("CUST001"))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Access denied");
    }
}