package com.minierpapp.model.customer;

import com.minierpapp.model.common.Status;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testCustomerCreation() {
        Customer customer = new Customer();
        customer.setCustomerCode("CUST001");
        customer.setName("Test Customer");
        customer.setStatus(Status.ACTIVE);
        customer.setCreatedAt(LocalDateTime.now());
        
        assertNotNull(customer);
        assertEquals("CUST001", customer.getCustomerCode());
        assertEquals("Test Customer", customer.getName());
        assertEquals(Status.ACTIVE, customer.getStatus());
        assertNotNull(customer.getCreatedAt());
        assertFalse(customer.isDeleted());
    }

    @Test
    void testCustomerValidation() {
        Customer customer = new Customer();
        customer.setStatus(null); // Explicitly set status to null to trigger validation
        
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("customerCode")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }

    @Test
    void testCustomerEquality() {
        Customer customer1 = new Customer();
        customer1.setId(1L);
        customer1.setCustomerCode("CUST001");
        customer1.setName("Test Customer");
        
        Customer customer2 = new Customer();
        customer2.setId(1L);
        customer2.setCustomerCode("CUST001");
        customer2.setName("Test Customer");
        
        assertTrue(customer1.equals(customer2));
        assertEquals(customer1.hashCode(), customer2.hashCode());
    }
}