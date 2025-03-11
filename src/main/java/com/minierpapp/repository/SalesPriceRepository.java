package com.minierpapp.repository;

import com.minierpapp.model.price.entity.SalesPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesPriceRepository extends JpaRepository<SalesPrice, Long> {
    
    List<SalesPrice> findByDeletedFalse();
    
    Optional<SalesPrice> findByIdAndDeletedFalse(Long id);
    
    List<SalesPrice> findByItemIdAndDeletedFalse(Long itemId);
    
    List<SalesPrice> findByCustomerIdAndDeletedFalse(Long customerId);
    
    List<SalesPrice> findByItemIdAndCustomerIdAndDeletedFalse(Long itemId, Long customerId);
    
    List<SalesPrice> findByItemCodeContainingAndCustomerCodeContainingAndDeletedFalse(
            String itemCode, String customerCode);
    
    List<SalesPrice> findByItemCodeContainingAndCustomerCodeContainingAndValidFromDateLessThanEqualAndValidToDateGreaterThanEqualAndDeletedFalse(
            String itemCode, String customerCode, LocalDate date1, LocalDate date2);
    
    List<SalesPrice> findByValidToDateLessThanAndDeletedFalse(LocalDate date);
    
    List<SalesPrice> findByValidToDateGreaterThanEqualAndValidToDateLessThanEqualAndDeletedFalse(
            LocalDate fromDate, LocalDate toDate);
    
    Optional<SalesPrice> findByItemCodeAndCustomerCodeAndValidFromDateLessThanEqualAndValidToDateGreaterThanEqualAndStatusAndDeletedFalse(
            String itemCode, String customerCode, LocalDate date1, LocalDate date2, String status);
    
    Optional<SalesPrice> findByItemCodeAndCustomerCodeIsNullAndValidFromDateLessThanEqualAndValidToDateGreaterThanEqualAndStatusAndDeletedFalse(
            String itemCode, LocalDate date1, LocalDate date2, String status);
    
    @Query("SELECT p FROM SalesPrice p " +
           "LEFT JOIN FETCH p.item " +
           "LEFT JOIN FETCH p.supplier " +
           "LEFT JOIN FETCH p.customer " +
           "WHERE p.deleted = false")
    List<SalesPrice> findAllWithRelations();
    
    @Query("SELECT p FROM SalesPrice p " +
           "LEFT JOIN FETCH p.item " +
           "LEFT JOIN FETCH p.supplier " +
           "LEFT JOIN FETCH p.customer " +
           "WHERE p.deleted = false " +
           "AND (:itemId IS NULL OR p.itemId = :itemId) " +
           "AND (:supplierId IS NULL OR p.supplierId = :supplierId) " +
           "AND (:customerId IS NULL OR p.customerId = :customerId)")
    List<SalesPrice> findWithFilters(@Param("itemId") Long itemId, 
                                    @Param("supplierId") Long supplierId, 
                                    @Param("customerId") Long customerId);
    
    @Query("SELECT p FROM SalesPrice p " +
           "LEFT JOIN FETCH p.item " +
           "LEFT JOIN FETCH p.supplier " +
           "LEFT JOIN FETCH p.customer " +
           "LEFT JOIN FETCH p.priceScales " +
           "WHERE p.id = :id AND p.deleted = false")
    Optional<SalesPrice> findByIdWithRelations(@Param("id") Long id);
} 