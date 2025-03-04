package com.minierpapp.service;

import com.minierpapp.model.price.entity.Price;
import com.minierpapp.repository.PriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceService {
    private final PriceRepository priceRepository;
    
    /**
     * 価格を保存する
     */
    @Transactional
    public Price save(Price price) {
        return priceRepository.save(price);
    }
    
    /**
     * 価格のリストを保存する
     */
    @Transactional
    public List<Price> saveAll(List<Price> prices) {
        return priceRepository.saveAll(prices);
    }
}