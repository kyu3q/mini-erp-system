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
    
    List<SalesPrice> findByItemIdAndCustomerIdIsNullAndDeletedFalse(Long itemId);
    
    /**
     * 重複する販売単価を検索する
     */
    @Query("SELECT p FROM SalesPrice p WHERE p.item.id = :itemId " +
           "AND (:customerId IS NULL AND p.customer IS NULL OR p.customer.id = :customerId) " +
           "AND NOT (p.validToDate < :fromDate OR p.validFromDate > :toDate)")
    List<SalesPrice> findOverlappingPrices(
        @Param("itemId") Long itemId,
        @Param("customerId") Long customerId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate);

    /**
     * 関連エンティティを含めた販売単価を検索する
     */
    @Query(value = "SELECT DISTINCT sp.* FROM prices sp " +
           "LEFT JOIN items i ON i.id = sp.item_id " +
           "LEFT JOIN customers c ON c.id = sp.customer_id " +
           "WHERE (:itemCode IS NULL OR :itemCode = '' OR i.item_code LIKE CONCAT('%', :itemCode, '%')) " +
           "AND (:itemName IS NULL OR :itemName = '' OR i.item_name LIKE CONCAT('%', :itemName, '%')) " +
           "AND (:customerCode IS NULL OR :customerCode = '' OR c.customer_code LIKE CONCAT('%', :customerCode, '%')) " +
           "AND (:customerName IS NULL OR :customerName = '' OR c.name LIKE CONCAT('%', :customerName, '%')) " +
           "AND (:validDate IS NULL OR :validDate = '' OR " +
           "    (sp.valid_from_date <= TO_DATE(:validDate, 'YYYY-MM-DD') AND " +
           "     sp.valid_to_date >= TO_DATE(:validDate, 'YYYY-MM-DD'))) " +
           "AND (:status IS NULL OR :status = '' OR sp.status = :status) " +
           "AND sp.deleted = false " +
           "AND sp.price_type = 'SALES'", 
           nativeQuery = true)
    List<SalesPrice> findAllWithItemAndCustomer(
        @Param("itemCode") String itemCode,
        @Param("itemName") String itemName,
        @Param("customerCode") String customerCode,
        @Param("customerName") String customerName,
        @Param("validDate") String validDate,
        @Param("status") String status
    );
} 