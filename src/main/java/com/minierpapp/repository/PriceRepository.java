package com.minierpapp.repository;

import com.minierpapp.model.price.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {
    
    /**
     * 削除されていない価格を全て取得
     */
    List<Price> findByDeletedFalse();
    
    /**
     * 特定のIDと削除フラグがfalseの価格を取得
     */
    Price findByIdAndDeletedFalse(Long id);
} 