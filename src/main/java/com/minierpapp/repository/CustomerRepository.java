package com.minierpapp.repository;

import com.minierpapp.model.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("SELECT c FROM Customer c WHERE c.deleted = false " +
           "AND (:customerCode IS NULL OR c.customerCode LIKE %:customerCode%) " +
           "AND (:name IS NULL OR c.name LIKE %:name%)")
    List<Customer> findByCustomerCodeAndName(@Param("customerCode") String customerCode,
                                           @Param("name") String name);

    Optional<Customer> findByCustomerCodeAndDeletedFalse(String customerCode);

    boolean existsByCustomerCodeAndDeletedFalse(String customerCode);

    List<Customer> findByDeletedFalse();

    List<Customer> findByCustomerCodeContainingOrNameContainingAndDeletedFalse(String customerCode, String name);
}