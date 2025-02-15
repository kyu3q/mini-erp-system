package com.minierpapp.repository;

import com.minierpapp.model.warehouse.Warehouse;
import com.minierpapp.model.common.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByWarehouseCode(String warehouseCode);
    boolean existsByWarehouseCode(String warehouseCode);

    @Query("SELECT w FROM Warehouse w WHERE " +
           "(:warehouseCode IS NULL OR w.warehouseCode LIKE %:warehouseCode%) AND " +
           "(:name IS NULL OR w.name LIKE %:name%)")
    Page<Warehouse> search(@Param("warehouseCode") String warehouseCode,
                         @Param("name") String name,
                         Pageable pageable);

    List<Warehouse> findByDeletedFalse();

    List<Warehouse> findByWarehouseCodeContainingIgnoreCaseOrNameContainingIgnoreCase(String keyword, String sameKeyword);

    List<Warehouse> findByStatusAndDeletedFalse(Status status);
}