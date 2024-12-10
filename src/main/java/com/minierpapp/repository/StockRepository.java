package com.minierpapp.repository;

import com.minierpapp.model.stock.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Page<Stock> findByWarehouseId(Long warehouseId, Pageable pageable);
    Page<Stock> findByItemId(Long itemId, Pageable pageable);
    Optional<Stock> findByWarehouseIdAndItemId(Long warehouseId, Long itemId);
    boolean existsByWarehouseIdAndItemId(Long warehouseId, Long itemId);
}