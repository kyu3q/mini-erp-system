package com.minierpapp.repository;

import com.minierpapp.model.order.Order;
import com.minierpapp.model.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN o.orderDetails d " +
           "WHERE o.deleted = false " +
           "AND (:orderNumber IS NULL OR o.orderNumber LIKE %:orderNumber%) " +
           "AND (:fromDate IS NULL OR o.orderDate >= :fromDate) " +
           "AND (:toDate IS NULL OR o.orderDate <= :toDate) " +
           "AND (:customerId IS NULL OR o.customer.id = :customerId) " +
           "AND (:productId IS NULL OR d.product.id = :productId) " +
           "AND (:status IS NULL OR o.status = :status)")
    List<Order> search(@Param("orderNumber") String orderNumber,
                      @Param("fromDate") LocalDate fromDate,
                      @Param("toDate") LocalDate toDate,
                      @Param("customerId") Long customerId,
                      @Param("productId") Long productId,
                      @Param("status") OrderStatus status);
}