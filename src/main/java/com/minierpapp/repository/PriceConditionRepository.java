package com.minierpapp.repository;

import com.minierpapp.model.price.PriceCondition;
import com.minierpapp.model.price.PriceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PriceConditionRepository extends JpaRepository<PriceCondition, Long> {
    @Query("SELECT p FROM PriceCondition p WHERE p.deleted = false " +
           "AND p.priceType = :priceType " +
           "AND p.item.id = :itemId " +
           "AND (:customerId IS NULL OR p.customer.id = :customerId) " +
           "AND (:supplierId IS NULL OR p.supplier.id = :supplierId) " +
           "AND :targetDate BETWEEN p.validFromDate AND p.validToDate " +
           "ORDER BY p.validFromDate DESC")
    List<PriceCondition> findValidPrices(
            @Param("priceType") PriceType priceType,
            @Param("itemId") Long itemId,
            @Param("customerId") Long customerId,
            @Param("supplierId") Long supplierId,
            @Param("targetDate") LocalDate targetDate);

    @Query("SELECT p FROM PriceCondition p WHERE p.deleted = false " +
           "AND p.priceType = :priceType " +
           "AND (:itemId IS NULL OR p.item.id = :itemId) " +
           "AND (:customerId IS NULL OR p.customer.id = :customerId) " +
           "AND (:supplierId IS NULL OR p.supplier.id = :supplierId) " +
           "ORDER BY p.validFromDate DESC")
    List<PriceCondition> findByConditions(
            @Param("priceType") PriceType priceType,
            @Param("itemId") Long itemId,
            @Param("customerId") Long customerId,
            @Param("supplierId") Long supplierId);
}