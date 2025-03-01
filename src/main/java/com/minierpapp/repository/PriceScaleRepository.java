package com.minierpapp.repository;

import com.minierpapp.model.price.entity.PriceScale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceScaleRepository extends JpaRepository<PriceScale, Long> {
    void deleteByPriceConditionId(Long priceConditionId);
    List<PriceScale> findByPriceConditionId(Long priceConditionId);
} 