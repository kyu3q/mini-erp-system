package com.minierpapp.repository;

import com.minierpapp.model.price.entity.SalesPrice;
import org.springframework.data.jpa.repository.JpaRepository;
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
} 