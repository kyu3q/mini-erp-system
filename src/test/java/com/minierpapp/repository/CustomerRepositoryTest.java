package com.minierpapp.repository;

import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.common.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void whenFindByCustomerCode_thenReturnCustomer() {
        // given
        Customer customer = new Customer();
        customer.setCustomerCode("CUST001");
        customer.setName("Test Customer");
        customer.setStatus(Status.ACTIVE);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        customer.setCreatedBy("system");
        customer.setUpdatedBy("system");
        entityManager.persist(customer);
        entityManager.flush();

        // when
        Optional<Customer> found = customerRepository.findByCustomerCodeAndDeletedFalse("CUST001");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getCustomerCode()).isEqualTo(customer.getCustomerCode());
        assertThat(found.get().getName()).isEqualTo(customer.getName());
    }

    @Test
    void whenFindByInvalidCustomerCode_thenReturnEmpty() {
        // when
        Optional<Customer> found = customerRepository.findByCustomerCodeAndDeletedFalse("INVALID");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void whenSaveCustomer_thenCustomerIsPersisted() {
        // given
        Customer customer = new Customer();
        customer.setCustomerCode("CUST002");
        customer.setName("New Customer");
        customer.setStatus(Status.ACTIVE);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        customer.setCreatedBy("system");
        customer.setUpdatedBy("system");

        // when
        Customer saved = customerRepository.save(customer);

        // then
        Customer found = entityManager.find(Customer.class, saved.getId());
        assertThat(found).isNotNull();
        assertThat(found.getCustomerCode()).isEqualTo(customer.getCustomerCode());
        assertThat(found.getName()).isEqualTo(customer.getName());
    }
}