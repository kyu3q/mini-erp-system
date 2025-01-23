package com.minierpapp.repository;

import com.minierpapp.model.common.Status;
import com.minierpapp.model.customer.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CustomerRepositoryIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void whenFindByCustomerCodeAndDeletedFalse_thenReturnCustomer() {
        // when
        Optional<Customer> found = customerRepository.findByCustomerCodeAndDeletedFalse("CUST001");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("テスト株式会社1");
    }

    @Test
    void whenFindByCustomerCodeAndName_thenReturnFilteredCustomers() {
        // when
        List<Customer> customers = customerRepository.findByCustomerCodeAndName("CUST", "テスト");

        // then
        assertThat(customers).hasSize(3);
        assertThat(customers).extracting(Customer::getCustomerCode)
                .containsExactlyInAnyOrder("CUST001", "CUST002", "CUST003");
    }

    @Test
    void whenFindByNameContainingAndDeletedFalse_thenReturnPagedResults() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by("name").ascending());

        // when
        Page<Customer> customerPage = customerRepository.findByNameContainingAndDeletedFalse("テスト", pageRequest);

        // then
        assertThat(customerPage.getContent()).hasSize(2);
        assertThat(customerPage.getTotalElements()).isEqualTo(3);
        assertThat(customerPage.getTotalPages()).isEqualTo(2);
    }

    @Test
    void whenFindByDeletedFalse_thenReturnActiveCustomers() {
        // when
        List<Customer> customers = customerRepository.findByDeletedFalse();

        // then
        assertThat(customers).hasSize(3);
        assertThat(customers).extracting(Customer::getDeleted)
                .containsOnly(false);
    }

    @Test
    void whenSaveCustomer_thenCustomerIsPersisted() {
        // given
        Customer newCustomer = new Customer();
        newCustomer.setCustomerCode("CUST004");
        newCustomer.setName("テスト株式会社4");
        newCustomer.setStatus(Status.ACTIVE);

        // when
        Customer saved = customerRepository.save(newCustomer);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCustomerCode()).isEqualTo("CUST004");
        assertThat(saved.getName()).isEqualTo("テスト株式会社4");
    }

    @Test
    void whenDeleteCustomer_thenCustomerIsMarkedAsDeleted() {
        // given
        Optional<Customer> customer = customerRepository.findByCustomerCodeAndDeletedFalse("CUST001");
        assertThat(customer).isPresent();

        // when
        customer.get().setDeleted(true);
        customerRepository.save(customer.get());

        // then
        Optional<Customer> deleted = customerRepository.findByCustomerCodeAndDeletedFalse("CUST001");
        assertThat(deleted).isEmpty();
    }

    @Test
    void whenExistsByCustomerCodeAndDeletedFalse_thenReturnTrue() {
        // when
        boolean exists = customerRepository.existsByCustomerCodeAndDeletedFalse("CUST001");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void whenFindByCustomerCodeContainingOrNameContainingAndDeletedFalse_thenReturnMatchingCustomers() {
        // when
        List<Customer> customers = customerRepository.findByCustomerCodeContainingOrNameContainingAndDeletedFalse("001", "2");

        // then
        assertThat(customers).hasSize(2);
        assertThat(customers).extracting(Customer::getCustomerCode)
                .containsExactlyInAnyOrder("CUST001", "CUST002");
    }
}