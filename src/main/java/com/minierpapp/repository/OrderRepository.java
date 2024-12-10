package com.minierpapp.repository;

import com.minierpapp.model.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o WHERE o.deleted = false")
    List<Order> findAllActive();

    @Query("SELECT o FROM Order o WHERE o.deleted = false AND o.id = :id")
    Optional<Order> findActiveById(Long id);

    @Query("SELECT o FROM Order o WHERE o.deleted = false AND o.orderNumber = :orderNumber")
    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(o.orderNumber, 9) AS int)), 0) FROM Order o WHERE o.orderNumber LIKE CONCAT(:prefix, '%')")
    Integer findMaxOrderNumberByPrefix(String prefix);
}