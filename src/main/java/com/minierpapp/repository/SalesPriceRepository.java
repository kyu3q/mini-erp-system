package com.minierpapp.repository;

import com.minierpapp.model.price.entity.PriceCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalesPriceRepository extends JpaRepository<PriceCondition, Long> {
    
    @Query("SELECT pc FROM PriceCondition pc WHERE pc.priceType = 'SALES' AND pc.deleted = false")
    List<PriceCondition> findAllSalesPrices();
    
    @Query("SELECT pc FROM PriceCondition pc WHERE pc.id = :id AND pc.priceType = 'SALES' AND pc.deleted = false")
    Optional<PriceCondition> findSalesPriceById(@Param("id") Long id);
    
    @Query("SELECT pc FROM PriceCondition pc WHERE pc.priceType = 'SALES' " +
           "AND pc.itemCode = :itemCode AND pc.customerCode = :customerCode " +
           "AND pc.validFromDate <= :date AND pc.validToDate >= :date " +
           "AND pc.status = 'ACTIVE' AND pc.deleted = false")
    Optional<PriceCondition> findActiveSalesPrice(@Param("itemCode") String itemCode, 
                                                @Param("customerCode") String customerCode,
                                                @Param("date") LocalDate date);
    
    @Query("SELECT pc FROM PriceCondition pc WHERE pc.priceType = 'SALES' " +
           "AND pc.itemCode = :itemCode AND pc.customer IS NULL " +
           "AND pc.validFromDate <= :date AND pc.validToDate >= :date " +
           "AND pc.status = 'ACTIVE' AND pc.deleted = false")
    Optional<PriceCondition> findActiveStandardSalesPrice(@Param("itemCode") String itemCode,
                                                        @Param("date") LocalDate date);
    
    @Query("SELECT pc FROM PriceCondition pc WHERE pc.priceType = 'SALES' " +
           "AND (:itemCode IS NULL OR pc.itemCode LIKE %:itemCode%) " +
           "AND (:customerCode IS NULL OR pc.customerCode LIKE %:customerCode%) " +
           "AND (:date IS NULL OR (pc.validFromDate <= :date AND pc.validToDate >= :date)) " +
           "AND pc.deleted = false " +
           "ORDER BY pc.itemCode, pc.customerCode")
    List<PriceCondition> searchSalesPrices(@Param("itemCode") String itemCode,
                                         @Param("customerCode") String customerCode,
                                         @Param("date") LocalDate date);
    
    @Query("SELECT pc FROM PriceCondition pc WHERE pc.priceType = 'SALES' " +
           "AND pc.validToDate < :date AND pc.deleted = false")
    List<PriceCondition> findExpiredSalesPrices(@Param("date") LocalDate date);
    
    @Query("SELECT pc FROM PriceCondition pc WHERE pc.priceType = 'SALES' " +
           "AND pc.validToDate >= :startDate AND pc.validToDate <= :endDate " +
           "AND pc.deleted = false")
    List<PriceCondition> findSoonExpiringPrices(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);
} 