package com.minierpapp.repository;

import com.minierpapp.model.supplier.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    @Query("SELECT s FROM Supplier s WHERE s.deleted = false " +
           "AND (:supplierCode IS NULL OR s.supplierCode LIKE %:supplierCode%) " +
           "AND (:name IS NULL OR s.name LIKE %:name%)")
    List<Supplier> findBySupplierCodeAndName(@Param("supplierCode") String supplierCode,
                                           @Param("name") String name);

    Optional<Supplier> findBySupplierCodeAndDeletedFalse(String supplierCode);

    boolean existsBySupplierCodeAndDeletedFalse(String supplierCode);

    List<Supplier> findBySupplierCodeContainingOrNameContainingAndDeletedFalse(String supplierCode, String name);

    List<Supplier> findByDeletedFalseOrderBySupplierCodeAsc();

    List<Supplier> findByDeletedFalse();

    Page<Supplier> findByDeletedFalse(Pageable pageable);

    Optional<Supplier> findBySupplierCode(String supplierCode);
}