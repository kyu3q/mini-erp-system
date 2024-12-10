package com.minierpapp.repository;

import com.minierpapp.model.item.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    
    Optional<Item> findByItemCodeAndDeletedFalse(String itemCode);
    
    List<Item> findByDeletedFalse();
    
    Page<Item> findByDeletedFalse(Pageable pageable);
    
    boolean existsByItemCodeAndDeletedFalse(String itemCode);

    List<Item> findByItemCodeContainingAndItemNameContainingAndDeletedFalse(String itemCode, String itemName);
}