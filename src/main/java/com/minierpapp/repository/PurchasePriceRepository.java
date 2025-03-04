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
} 