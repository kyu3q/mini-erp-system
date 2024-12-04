package com.minierpapp.repository;

import com.minierpapp.model.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findByProductCodeAndDeletedFalse(String productCode);
    
    List<Product> findByDeletedFalse();
    
    boolean existsByProductCodeAndDeletedFalse(String productCode);
}