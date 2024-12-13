package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.common.Status;
import com.minierpapp.model.price.*;
import com.minierpapp.model.price.dto.*;
import com.minierpapp.model.price.mapper.PriceMapper;
import com.minierpapp.repository.PriceRepository;
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
    private final PriceRepository priceRepository;
    private final PriceMapper priceMapper;

    /**
     * 全ての単価マスタを取得する
     */
    @Transactional(readOnly = true)
    public List<PriceResponse> findAll() {
        return priceRepository.findAll().stream()
            .map(priceMapper::toResponse)
            .toList();
    }

    /**
     * 単価マスタを登録する
     */
    public PriceResponse createPrice(PriceRequest request) {
        validatePriceCondition(request);

        Price price = priceMapper.toEntity(request);

        switch (request.getConditionType()) {
            case ITEM_ONLY -> setItemPrices(price, request.getPriceItems());
            case SUPPLIER_ITEM -> setSupplierItemPrices(price, request.getPriceSupplierItems());
            case CUSTOMER_ITEM -> setCustomerItemPrices(price, request.getPriceCustomerItems());
            case SUPPLIER_CUSTOMER_ITEM -> setSupplierCustomerItemPrices(price, request.getPriceSupplierCustomerItems());
        }

        return priceMapper.toResponse(priceRepository.save(price));
    }

    /**
     * 単価マスタを更新する
     */
    public PriceResponse updatePrice(Long id, PriceRequest request) {
        validatePriceCondition(request);

        Price price = priceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Price not found with id: " + id));

        priceMapper.updateEntity(price, request);

        switch (request.getConditionType()) {
            case ITEM_ONLY -> setItemPrices(price, request.getPriceItems());
            case SUPPLIER_ITEM -> setSupplierItemPrices(price, request.getPriceSupplierItems());
            case CUSTOMER_ITEM -> setCustomerItemPrices(price, request.getPriceCustomerItems());
            case SUPPLIER_CUSTOMER_ITEM -> setSupplierCustomerItemPrices(price, request.getPriceSupplierCustomerItems());
        }

        return priceMapper.toResponse(priceRepository.save(price));
    }

    /**
     * 単価マスタを取得する
     */
    public PriceResponse getPrice(Long id) {
        return priceMapper.toResponse(priceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Price not found with id: " + id)));
    }

    /**
     * 単価マスタを削除する
     */
    public void deletePrice(Long id) {
        Price price = priceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Price not found with id: " + id));
        price.setStatus(Status.INACTIVE);
        priceRepository.save(price);
    }

    /**
     * 販売単価を取得する
     */
    public BigDecimal getSalesPrice(
            String itemCode,
            String customerCode,
            BigDecimal quantity,
            LocalDate priceDate) {

        List<Price> validPrices = priceRepository.findValidSalesPrices(
                PriceType.SALES, itemCode, customerCode, priceDate);

        if (validPrices.isEmpty()) {
            throw new ResourceNotFoundException("有効な価格が見つかりません");
        }

        // 優先順位：得意先・品目 > 品目のみ
        return validPrices.stream()
            .filter(price -> price.getConditionType() == ConditionType.CUSTOMER_ITEM)
            .findFirst()
            .map(price -> findCustomerItemPrice(price, customerCode, itemCode, quantity))
            .orElseGet(() ->
                validPrices.stream()
                    .filter(price -> price.getConditionType() == ConditionType.ITEM_ONLY)
                    .findFirst()
                    .map(price -> findItemPrice(price, itemCode, quantity))
                    .orElseThrow(() -> new ResourceNotFoundException("有効な価格が見つかりません"))
            );
    }

    /**
     * 購買単価を取得する
     */
    public BigDecimal getPurchasePrice(
            String itemCode,
            String supplierCode,
            String customerCode,
            BigDecimal quantity,
            LocalDate priceDate) {

        List<Price> validPrices = priceRepository.findValidPurchasePrices(
                PriceType.PURCHASE, itemCode, supplierCode, customerCode, priceDate);

        if (validPrices.isEmpty()) {
            throw new ResourceNotFoundException("有効な価格が見つかりません");
        }

        // 優先順位：仕入先・得意先・品目 > 仕入先・品目 > 品目のみ
        return validPrices.stream()
            .filter(price -> price.getConditionType() == ConditionType.SUPPLIER_CUSTOMER_ITEM)
            .findFirst()
            .map(price -> findSupplierCustomerItemPrice(price, supplierCode, customerCode, itemCode, quantity))
            .orElseGet(() -> 
                validPrices.stream()
                    .filter(price -> price.getConditionType() == ConditionType.SUPPLIER_ITEM)
                    .findFirst()
                    .map(price -> findSupplierItemPrice(price, supplierCode, itemCode, quantity))
                    .orElseGet(() ->
                        validPrices.stream()
                            .filter(price -> price.getConditionType() == ConditionType.ITEM_ONLY)
                            .findFirst()
                            .map(price -> findItemPrice(price, itemCode, quantity))
                            .orElseThrow(() -> new ResourceNotFoundException("有効な価格が見つかりません"))
                    )
            );
    }

    private void validatePriceCondition(PriceRequest request) {
        // 販売単価の場合、仕入先関連の条件タイプは使用不可
        if (request.getPriceType() == PriceType.SALES) {
            if (request.getConditionType() == ConditionType.SUPPLIER_ITEM ||
                request.getConditionType() == ConditionType.SUPPLIER_CUSTOMER_ITEM) {
                throw new IllegalArgumentException("販売単価では仕入先関連の条件タイプは使用できません");
            }
        }

        // 有効期間の妥当性チェック
        if (request.getValidFromDate().isAfter(request.getValidToDate())) {
            throw new IllegalArgumentException("有効開始日は有効終了日以前である必要があります");
        }
    }

    private BigDecimal findItemPrice(Price price, String itemCode, BigDecimal quantity) {
        return price.getPriceItems().stream()
            .filter(p -> p.getItemCode().equals(itemCode))
            .findFirst()
            .map(p -> findScalePrice(p.getPriceScales(), quantity, p.getBasePrice()))
            .orElseThrow(() -> new ResourceNotFoundException("適用可能な価格が見つかりません"));
    }

    private BigDecimal findSupplierItemPrice(Price price, String supplierCode, String itemCode, BigDecimal quantity) {
        return price.getPriceSupplierItems().stream()
            .filter(p -> p.getSupplierCode().equals(supplierCode) && p.getItemCode().equals(itemCode))
            .findFirst()
            .map(p -> findScalePrice(p.getPriceScales(), quantity, p.getBasePrice()))
            .orElseThrow(() -> new ResourceNotFoundException("適用可能な価格が見つかりません"));
    }

    private BigDecimal findCustomerItemPrice(Price price, String customerCode, String itemCode, BigDecimal quantity) {
        return price.getPriceCustomerItems().stream()
            .filter(p -> p.getCustomerCode().equals(customerCode) && p.getItemCode().equals(itemCode))
            .findFirst()
            .map(p -> findScalePrice(p.getPriceScales(), quantity, p.getBasePrice()))
            .orElseThrow(() -> new ResourceNotFoundException("適用可能な価格が見つかりません"));
    }

    private BigDecimal findSupplierCustomerItemPrice(
            Price price,
            String supplierCode,
            String customerCode,
            String itemCode,
            BigDecimal quantity) {
        return price.getPriceSupplierCustomerItems().stream()
            .filter(p -> 
                p.getSupplierCode().equals(supplierCode) &&
                p.getCustomerCode().equals(customerCode) &&
                p.getItemCode().equals(itemCode))
            .findFirst()
            .map(p -> findScalePrice(p.getPriceScales(), quantity, p.getBasePrice()))
            .orElseThrow(() -> new ResourceNotFoundException("適用可能な価格が見つかりません"));
    }

    private BigDecimal findScalePrice(List<PriceScale> scales, BigDecimal quantity, BigDecimal basePrice) {
        return scales.stream()
            .filter(scale -> 
                scale.getFromQuantity().compareTo(quantity) <= 0 &&
                scale.getToQuantity().compareTo(quantity) >= 0)
            .findFirst()
            .map(PriceScale::getScalePrice)
            .orElse(basePrice);
    }

    private void setItemPrices(Price price, List<PriceItemRequest> requests) {
        if (requests == null) return;
        price.getPriceItems().clear();
        requests.forEach(request -> {
            PriceItem item = priceMapper.toEntity(request);
            item.setPrice(price);
            price.getPriceItems().add(item);
        });
    }

    private void setSupplierItemPrices(Price price, List<PriceSupplierItemRequest> requests) {
        if (requests == null) return;
        price.getPriceSupplierItems().clear();
        requests.forEach(request -> {
            PriceSupplierItem item = priceMapper.toEntity(request);
            item.setPrice(price);
            price.getPriceSupplierItems().add(item);
        });
    }

    private void setCustomerItemPrices(Price price, List<PriceCustomerItemRequest> requests) {
        if (requests == null) return;
        price.getPriceCustomerItems().clear();
        requests.forEach(request -> {
            PriceCustomerItem item = priceMapper.toEntity(request);
            item.setPrice(price);
            price.getPriceCustomerItems().add(item);
        });
    }

    private void setSupplierCustomerItemPrices(Price price, List<PriceSupplierCustomerItemRequest> requests) {
        if (requests == null) return;
        price.getPriceSupplierCustomerItems().clear();
        requests.forEach(request -> {
            PriceSupplierCustomerItem item = priceMapper.toEntity(request);
            item.setPrice(price);
            price.getPriceSupplierCustomerItems().add(item);
        });
    }

    @Transactional
    public PriceResponse duplicatePrice(Long id) {
        Price originalPrice = priceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Price not found with id: " + id));

        Price newPrice = new Price();
        newPrice.setPriceType(originalPrice.getPriceType());
        newPrice.setConditionType(originalPrice.getConditionType());
        newPrice.setValidFromDate(originalPrice.getValidFromDate());
        newPrice.setValidToDate(originalPrice.getValidToDate());
        newPrice.setStatus(Status.ACTIVE);

        // 品目単価のコピー
        originalPrice.getPriceItems().forEach(item -> {
            PriceItem newItem = new PriceItem();
            newItem.setPrice(newPrice);
            newItem.setItemCode(item.getItemCode());
            newItem.setBasePrice(item.getBasePrice());
            newItem.setCurrencyCode(item.getCurrencyCode());

            // 数量スケール価格のコピー
            item.getPriceScales().forEach(scale -> {
                PriceScale newScale = new PriceScale();
                // newScale.setPriceItem(newItem);
                newScale.setFromQuantity(scale.getFromQuantity());
                newScale.setToQuantity(scale.getToQuantity());
                newScale.setScalePrice(scale.getScalePrice());
                newItem.getPriceScales().add(newScale);
            });

            newPrice.getPriceItems().add(newItem);
        });

        // 仕入先・品目単価のコピー
        originalPrice.getPriceSupplierItems().forEach(item -> {
            PriceSupplierItem newItem = new PriceSupplierItem();
            newItem.setPrice(newPrice);
            newItem.setSupplierCode(item.getSupplierCode());
            newItem.setItemCode(item.getItemCode());
            newItem.setBasePrice(item.getBasePrice());
            newItem.setCurrencyCode(item.getCurrencyCode());

            // 数量スケール価格のコピー
            item.getPriceScales().forEach(scale -> {
                PriceScale newScale = new PriceScale();
                // newScale.setPriceSupplierItem(newItem);
                newScale.setFromQuantity(scale.getFromQuantity());
                newScale.setToQuantity(scale.getToQuantity());
                newScale.setScalePrice(scale.getScalePrice());
                newItem.getPriceScales().add(newScale);
            });

            newPrice.getPriceSupplierItems().add(newItem);
        });

        // 得意先・品目単価のコピー
        originalPrice.getPriceCustomerItems().forEach(item -> {
            PriceCustomerItem newItem = new PriceCustomerItem();
            newItem.setPrice(newPrice);
            newItem.setCustomerCode(item.getCustomerCode());
            newItem.setItemCode(item.getItemCode());
            newItem.setBasePrice(item.getBasePrice());
            newItem.setCurrencyCode(item.getCurrencyCode());

            // 数量スケール価格のコピー
            item.getPriceScales().forEach(scale -> {
                PriceScale newScale = new PriceScale();
                // newScale.setPriceCustomerItem(newItem);
                newScale.setFromQuantity(scale.getFromQuantity());
                newScale.setToQuantity(scale.getToQuantity());
                newScale.setScalePrice(scale.getScalePrice());
                newItem.getPriceScales().add(newScale);
            });

            newPrice.getPriceCustomerItems().add(newItem);
        });

        // 仕入先・得意先・品目単価のコピー
        originalPrice.getPriceSupplierCustomerItems().forEach(item -> {
            PriceSupplierCustomerItem newItem = new PriceSupplierCustomerItem();
            newItem.setPrice(newPrice);
            newItem.setSupplierCode(item.getSupplierCode());
            newItem.setCustomerCode(item.getCustomerCode());
            newItem.setItemCode(item.getItemCode());
            newItem.setBasePrice(item.getBasePrice());
            newItem.setCurrencyCode(item.getCurrencyCode());

            // 数量スケール価格のコピー
            item.getPriceScales().forEach(scale -> {
                PriceScale newScale = new PriceScale();
                // newScale.setPriceSupplierCustomerItem(newItem);
                newScale.setFromQuantity(scale.getFromQuantity());
                newScale.setToQuantity(scale.getToQuantity());
                newScale.setScalePrice(scale.getScalePrice());
                newItem.getPriceScales().add(newScale);
            });

            newPrice.getPriceSupplierCustomerItems().add(newItem);
        });

        return priceMapper.toResponse(priceRepository.save(newPrice));
    }
}