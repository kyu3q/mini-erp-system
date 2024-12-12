package com.minierpapp.repository;

import com.minierpapp.model.price.Price;
import com.minierpapp.model.price.PriceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface PriceRepository extends JpaRepository<Price, Long> {
    @Query("SELECT p FROM Price p " +
           "WHERE p.priceType = :priceType " +
           "AND p.validFromDate <= :priceDate " +
           "AND p.validToDate >= :priceDate " +
           "AND p.status = 'ACTIVE' " +
           "AND (" +
           "    (p.conditionType = 'ITEM_ONLY' AND EXISTS (SELECT 1 FROM PriceItem pi WHERE pi.price = p AND pi.itemCode = :itemCode AND pi.status = 'ACTIVE')) " +
           "    OR (p.conditionType = 'CUSTOMER_ITEM' AND EXISTS (SELECT 1 FROM PriceCustomerItem pci WHERE pci.price = p AND pci.itemCode = :itemCode AND pci.customerCode = :customerCode AND pci.status = 'ACTIVE')) " +
           ") " +
           "ORDER BY p.conditionType DESC")
    List<Price> findValidSalesPrices(
            @Param("priceType") PriceType priceType,
            @Param("itemCode") String itemCode,
            @Param("customerCode") String customerCode,
            @Param("priceDate") LocalDate priceDate);

    @Query("SELECT p FROM Price p " +
           "WHERE p.priceType = :priceType " +
           "AND p.validFromDate <= :priceDate " +
           "AND p.validToDate >= :priceDate " +
           "AND p.status = 'ACTIVE' " +
           "AND (" +
           "    (p.conditionType = 'ITEM_ONLY' AND EXISTS (SELECT 1 FROM PriceItem pi WHERE pi.price = p AND pi.itemCode = :itemCode AND pi.status = 'ACTIVE')) " +
           "    OR (p.conditionType = 'SUPPLIER_ONLY' AND EXISTS (SELECT 1 FROM PriceSupplier ps WHERE ps.price = p AND ps.supplierCode = :supplierCode AND ps.status = 'ACTIVE')) " +
           "    OR (p.conditionType = 'SUPPLIER_ITEM' AND EXISTS (SELECT 1 FROM PriceSupplierItem psi WHERE psi.price = p AND psi.itemCode = :itemCode AND psi.supplierCode = :supplierCode AND psi.status = 'ACTIVE')) " +
           "    OR (p.conditionType = 'SUPPLIER_CUSTOMER_ITEM' AND EXISTS (SELECT 1 FROM PriceSupplierCustomerItem psci WHERE psci.price = p AND psci.itemCode = :itemCode AND psci.supplierCode = :supplierCode AND psci.customerCode = :customerCode AND psci.status = 'ACTIVE')) " +
           ") " +
           "ORDER BY p.conditionType DESC")
    List<Price> findValidPurchasePrices(
            @Param("priceType") PriceType priceType,
            @Param("itemCode") String itemCode,
            @Param("supplierCode") String supplierCode,
            @Param("customerCode") String customerCode,
            @Param("priceDate") LocalDate priceDate);
}