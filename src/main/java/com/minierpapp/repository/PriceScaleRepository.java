package com.minierpapp.repository;

import com.minierpapp.model.price.entity.PriceScale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceScaleRepository extends JpaRepository<PriceScale, Long> {
    
    /**
     * 特定の価格IDに関連する価格スケールを削除
     */
    void deleteByPriceId(Long priceId);
    
    List<PriceScale> findByPriceId(Long priceId);

    List<PriceScale> findByPriceIdAndDeletedFalse(Long priceId);
} 