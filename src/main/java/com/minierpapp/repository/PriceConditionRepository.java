package com.minierpapp.repository;

import com.minierpapp.model.price.entity.PriceType;
import com.minierpapp.model.price.entity.PriceCondition;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
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
    List<PriceCondition> findByPriceTypeAndDeletedFalse(PriceType priceType);

    Optional<PriceCondition> findByIdAndDeletedFalse(Long id);
    
    @Query("SELECT p FROM PriceCondition p WHERE p.priceType = 'SALES' AND p.deleted = false " +
           "AND (:itemCode IS NULL OR p.itemCode = :itemCode) " +
           "AND (:customerCode IS NULL OR p.customerCode = :customerCode) " +
           "AND p.validFromDate <= :date AND p.validToDate >= :date " +
           "AND p.status = 'ACTIVE' ORDER BY p.customerCode DESC")
    List<PriceCondition> findActiveSalesPrices(@Param("itemCode") String itemCode, 
                                              @Param("customerCode") String customerCode, 
                                              @Param("date") LocalDate date);
    
    @Query("SELECT p FROM PriceCondition p WHERE p.priceType = 'PURCHASE' AND p.deleted = false " +
           "AND (:itemCode IS NULL OR p.itemCode = :itemCode) " +
           "AND (:supplierCode IS NULL OR p.supplierCode = :supplierCode) " +
           "AND p.validFromDate <= :date AND p.validToDate >= :date " +
           "AND p.status = 'ACTIVE' ORDER BY p.supplierCode DESC")
    List<PriceCondition> findActivePurchasePrices(@Param("itemCode") String itemCode, 
                                                 @Param("supplierCode") String supplierCode, 
                                                 @Param("date") LocalDate date);
    
    @Query("SELECT p FROM PriceCondition p WHERE p.priceType = 'SALES' AND p.deleted = false " +
           "AND p.itemCode = :itemCode AND p.customerCode = :customerCode " +
           "AND p.validFromDate <= :date AND p.validToDate >= :date " +
           "AND p.status = 'ACTIVE'")
    Optional<PriceCondition> findActiveSalesPrice(@Param("itemCode") String itemCode, 
                                                @Param("customerCode") String customerCode, 
                                                @Param("date") LocalDate date);
    
    @Query("SELECT p FROM PriceCondition p WHERE p.priceType = 'SALES' AND p.deleted = false " +
           "AND p.itemCode = :itemCode AND p.customerCode IS NULL " +
           "AND p.validFromDate <= :date AND p.validToDate >= :date " +
           "AND p.status = 'ACTIVE'")
    Optional<PriceCondition> findActiveStandardSalesPrice(@Param("itemCode") String itemCode, 
                                                        @Param("date") LocalDate date);
    
    @Query("SELECT p FROM PriceCondition p WHERE p.priceType = 'PURCHASE' AND p.deleted = false " +
           "AND p.itemCode = :itemCode AND p.supplierCode = :supplierCode " +
           "AND p.validFromDate <= :date AND p.validToDate >= :date " +
           "AND p.status = 'ACTIVE'")
    Optional<PriceCondition> findActivePurchasePrice(@Param("itemCode") String itemCode, 
                                                   @Param("supplierCode") String supplierCode, 
                                                   @Param("date") LocalDate date);
    
    @Query("SELECT p FROM PriceCondition p WHERE p.priceType = 'PURCHASE' AND p.deleted = false " +
           "AND p.itemCode = :itemCode AND p.supplierCode IS NULL " +
           "AND p.validFromDate <= :date AND p.validToDate >= :date " +
           "AND p.status = 'ACTIVE'")
    Optional<PriceCondition> findActiveStandardPurchasePrice(@Param("itemCode") String itemCode, 
                                                           @Param("date") LocalDate date);

    Optional<PriceCondition> findByIdAndPriceTypeAndDeletedFalse(Long id, PriceType priceType);
    List<PriceCondition> findByPriceTypeAndItemIdAndSupplierIdAndDeletedFalse(PriceType priceType, Long itemId, Long supplierId);
    List<PriceCondition> findByPriceTypeAndItemIdAndDeletedFalse(PriceType priceType, Long itemId);
    List<PriceCondition> findByPriceTypeAndSupplierIdAndDeletedFalse(PriceType priceType, Long supplierId);
}