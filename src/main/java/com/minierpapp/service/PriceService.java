package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.common.Status;
import com.minierpapp.model.price.PriceCondition;
import com.minierpapp.model.price.PriceType;
import com.minierpapp.model.price.dto.PriceConditionRequest;
import com.minierpapp.model.price.dto.PriceConditionResponse;
import com.minierpapp.model.price.mapper.PriceMapper;
import com.minierpapp.repository.PriceConditionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PriceService {
    private final PriceConditionRepository priceConditionRepository;
    private final PriceMapper priceMapper;

    @Transactional(readOnly = true)
    public List<PriceCondition> findAllSalesPrices() {
        return priceConditionRepository.findByPriceTypeAndDeletedFalse(PriceType.SALES);
    }

    @Transactional(readOnly = true)
    public List<PriceCondition> findAllPurchasePrices() {
        return priceConditionRepository.findByPriceTypeAndDeletedFalse(PriceType.PURCHASE);
    }

    public PriceConditionResponse createPrice(PriceConditionRequest request) {
        validatePriceCondition(request);

        PriceCondition price = priceMapper.toEntity(request);
        return priceMapper.toResponse(priceConditionRepository.save(price));
    }

    public PriceConditionResponse updatePrice(Long id, PriceConditionRequest request) {
        validatePriceCondition(request);

        PriceCondition price = priceConditionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Price not found with id: " + id));

        priceMapper.updateEntity(price, request);
        return priceMapper.toResponse(priceConditionRepository.save(price));
    }

    @Transactional(readOnly = true)
    public PriceConditionResponse getPrice(Long id) {
        return priceMapper.toResponse(priceConditionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Price not found with id: " + id)));
    }

    public void deletePrice(Long id) {
        PriceCondition price = priceConditionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Price not found with id: " + id));
        price.setStatus(Status.INACTIVE);
        price.setDeleted(true);
        priceConditionRepository.save(price);
    }

    public BigDecimal getSalesPrice(Long itemId, Long customerId, BigDecimal quantity, LocalDate priceDate) {
        List<PriceCondition> validPrices = priceConditionRepository.findValidPrices(
                PriceType.SALES, itemId, customerId, null, priceDate);

        if (validPrices.isEmpty()) {
            throw new ResourceNotFoundException("有効な価格が見つかりません");
        }

        // 優先順位：得意先個別価格 > 品目基本価格
        return validPrices.stream()
                .filter(price -> price.getCustomer() != null)
                .findFirst()
                .map(price -> findApplicablePrice(price, quantity))
                .orElseGet(() ->
                        validPrices.stream()
                                .filter(price -> price.getCustomer() == null)
                                .findFirst()
                                .map(price -> findApplicablePrice(price, quantity))
                                .orElseThrow(() -> new ResourceNotFoundException("有効な価格が見つかりません"))
                );
    }

    public BigDecimal getPurchasePrice(Long itemId, Long supplierId, Long customerId, BigDecimal quantity, LocalDate priceDate) {
        List<PriceCondition> validPrices = priceConditionRepository.findValidPrices(
                PriceType.PURCHASE, itemId, customerId, supplierId, priceDate);

        if (validPrices.isEmpty()) {
            throw new ResourceNotFoundException("有効な価格が見つかりません");
        }

        // 優先順位：仕入先・得意先個別価格 > 仕入先個別価格 > 品目基本価格
        return validPrices.stream()
                .filter(price -> price.getSupplier() != null && price.getCustomer() != null)
                .findFirst()
                .map(price -> findApplicablePrice(price, quantity))
                .orElseGet(() ->
                        validPrices.stream()
                                .filter(price -> price.getSupplier() != null && price.getCustomer() == null)
                                .findFirst()
                                .map(price -> findApplicablePrice(price, quantity))
                                .orElseGet(() ->
                                        validPrices.stream()
                                                .filter(price -> price.getSupplier() == null && price.getCustomer() == null)
                                                .findFirst()
                                                .map(price -> findApplicablePrice(price, quantity))
                                                .orElseThrow(() -> new ResourceNotFoundException("有効な価格が見つかりません"))
                                )
                );
    }

    private BigDecimal findApplicablePrice(PriceCondition price, BigDecimal quantity) {
        return price.getPriceScales().stream()
                .filter(scale ->
                        scale.getFromQuantity().compareTo(quantity) <= 0 &&
                                (scale.getToQuantity() == null || scale.getToQuantity().compareTo(quantity) >= 0))
                .findFirst()
                .map(scale -> scale.getScalePrice())
                .orElse(price.getBasePrice());
    }

    private void validatePriceCondition(PriceConditionRequest request) {
        // 販売単価の場合、仕入先指定は不可
        if (request.getPriceType() == PriceType.SALES && request.getSupplierId() != null) {
            throw new IllegalArgumentException("販売単価に仕入先は指定できません");
        }

        // 購買単価の場合、仕入先は必須
        if (request.getPriceType() == PriceType.PURCHASE && request.getSupplierId() == null) {
            throw new IllegalArgumentException("購買単価の場合、仕入先の指定は必須です");
        }

        // 有効期間の妥当性チェック
        if (request.getValidFromDate().isAfter(request.getValidToDate())) {
            throw new IllegalArgumentException("有効開始日は有効終了日以前である必要があります");
        }

        // スケール価格のチェック
        if (request.getPriceScales() != null && !request.getPriceScales().isEmpty()) {
            validatePriceScales(request);
        }
    }

    private void validatePriceScales(PriceConditionRequest request) {
        BigDecimal previousToQuantity = null;
        for (int i = 0; i < request.getPriceScales().size(); i++) {
            var scale = request.getPriceScales().get(i);

            // スケール価格は基本価格以下であること
            if (scale.getScalePrice().compareTo(request.getBasePrice()) > 0) {
                throw new IllegalArgumentException("スケール価格は基本価格以下である必要があります");
            }

            // 開始数量は前の終了数量より大きいこと
            if (previousToQuantity != null && scale.getFromQuantity().compareTo(previousToQuantity) <= 0) {
                throw new IllegalArgumentException("スケール価格の数量範囲が重複しています");
            }

            // 終了数量は開始数量より大きいこと
            if (scale.getToQuantity() != null && scale.getFromQuantity().compareTo(scale.getToQuantity()) >= 0) {
                throw new IllegalArgumentException("終了数量は開始数量より大きい必要があります");
            }

            previousToQuantity = scale.getToQuantity();
        }
    }
}