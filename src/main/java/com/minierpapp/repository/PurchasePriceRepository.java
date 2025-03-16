package com.minierpapp.repository;

import com.minierpapp.model.price.entity.PurchasePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchasePriceRepository extends JpaRepository<PurchasePrice, Long> {
    
    List<PurchasePrice> findByDeletedFalse();
    
    Optional<PurchasePrice> findByIdAndDeletedFalse(Long id);
    
    List<PurchasePrice> findByItemIdAndDeletedFalse(Long itemId);
    
    List<PurchasePrice> findBySupplierIdAndDeletedFalse(Long supplierId);
    
    List<PurchasePrice> findByItemIdAndSupplierIdAndDeletedFalse(Long itemId, Long supplierId);
    
//     @Query("SELECT p FROM PurchasePrice p " +
//            "LEFT JOIN FETCH p.item " +
//            "LEFT JOIN FETCH p.supplier " +
//            "LEFT JOIN FETCH p.customer " +
//            "WHERE p.deleted = false")
//     List<PurchasePrice> findAllWithRelations();
    
//     @Query("SELECT p FROM PurchasePrice p " +
//            "LEFT JOIN FETCH p.item " +
//            "LEFT JOIN FETCH p.supplier " +
//            "LEFT JOIN FETCH p.customer " +
//            "WHERE p.deleted = false " +
//            "AND (:itemId IS NULL OR p.itemId = :itemId) " +
//            "AND (:supplierId IS NULL OR p.supplierId = :supplierId) " +
//            "AND (:customerId IS NULL OR p.customerId = :customerId)")
//     List<PurchasePrice> findWithFilters(@Param("itemId") Long itemId, 
//                                        @Param("supplierId") Long supplierId, 
//                                        @Param("customerId") Long customerId);
    
//     @Query("SELECT p FROM PurchasePrice p " +
//            "LEFT JOIN FETCH p.item " +
//            "LEFT JOIN FETCH p.supplier " +
//            "LEFT JOIN FETCH p.customer " +
//            "LEFT JOIN FETCH p.priceScales " +
//            "WHERE p.id = :id AND p.deleted = false")
//     Optional<PurchasePrice> findByIdWithRelations(@Param("id") Long id);
} 