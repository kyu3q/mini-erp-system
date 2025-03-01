package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.price.dto.PurchasePriceRequest;
import com.minierpapp.model.price.dto.PurchasePriceResponse;
import com.minierpapp.model.price.entity.PriceCondition;
import com.minierpapp.model.price.entity.PriceType;
import com.minierpapp.model.price.entity.PriceScale;
import com.minierpapp.model.price.mapper.PurchasePriceMapper;
import com.minierpapp.model.supplier.Supplier;
import com.minierpapp.repository.ItemRepository;
import com.minierpapp.repository.PriceConditionRepository;
import com.minierpapp.repository.PriceScaleRepository;
import com.minierpapp.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchasePriceService {
    private final PriceConditionRepository priceConditionRepository;
    private final PriceScaleRepository priceScaleRepository;
    private final ItemRepository itemRepository;
    private final SupplierRepository supplierRepository;
    private final PurchasePriceMapper purchasePriceMapper;

    @Transactional(readOnly = true)
    public List<PurchasePriceResponse> findAll() {
        return purchasePriceMapper.toResponseList(
            priceConditionRepository.findByPriceTypeAndDeletedFalse(PriceType.PURCHASE)
        );
    }

    @Transactional(readOnly = true)
    public PurchasePriceResponse findById(Long id) {
        return purchasePriceMapper.entityToResponse(
            priceConditionRepository.findByIdAndPriceTypeAndDeletedFalse(id, PriceType.PURCHASE)
                .orElseThrow(() -> new ResourceNotFoundException("購買価格", id))
        );
    }

    @Transactional(readOnly = true)
    public List<PurchasePriceResponse> search(Long itemId, Long supplierId) {
        List<PriceCondition> prices;
        
        if (itemId != null && supplierId != null) {
            prices = priceConditionRepository.findByPriceTypeAndItemIdAndSupplierIdAndDeletedFalse(
                PriceType.PURCHASE, itemId, supplierId);
        } else if (itemId != null) {
            prices = priceConditionRepository.findByPriceTypeAndItemIdAndDeletedFalse(
                PriceType.PURCHASE, itemId);
        } else if (supplierId != null) {
            prices = priceConditionRepository.findByPriceTypeAndSupplierIdAndDeletedFalse(
                PriceType.PURCHASE, supplierId);
        } else {
            prices = priceConditionRepository.findByPriceTypeAndDeletedFalse(PriceType.PURCHASE);
        }
        
        return purchasePriceMapper.toResponseList(prices);
    }

    @Transactional
    public void create(PurchasePriceRequest request) {
        validateRequest(request);
        
        PriceCondition priceCondition = purchasePriceMapper.requestToEntity(request);
        priceCondition.setPriceType(PriceType.PURCHASE);
        
        // 関連エンティティの設定
        if (request.getItemId() != null) {
            Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("品目", request.getItemId()));
            priceCondition.setItem(item);
            priceCondition.setItemCode(item.getItemCode());
        } else if (request.getItemCode() != null && !request.getItemCode().isEmpty()) {
            Item item = itemRepository.findByItemCode(request.getItemCode())
                .orElseThrow(() -> new ResourceNotFoundException("品目コード", request.getItemCode()));
            priceCondition.setItem(item);
            priceCondition.setItemId(item.getId());
            priceCondition.setItemCode(item.getItemCode());
        }
        
        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("仕入先", request.getSupplierId()));
            priceCondition.setSupplier(supplier);
            priceCondition.setSupplierCode(supplier.getSupplierCode());
        } else if (request.getSupplierCode() != null && !request.getSupplierCode().isEmpty()) {
            Supplier supplier = supplierRepository.findBySupplierCode(request.getSupplierCode())
                .orElseThrow(() -> new ResourceNotFoundException("仕入先コード", request.getSupplierCode()));
            priceCondition.setSupplier(supplier);
            priceCondition.setSupplierId(supplier.getId());
            priceCondition.setSupplierCode(supplier.getSupplierCode());
        }
        
        priceConditionRepository.save(priceCondition);
        
        // 数量スケールの保存
        if (request.getPriceScales() != null && !request.getPriceScales().isEmpty()) {
            List<PriceScale> scales = request.getPriceScales().stream()
                .map(scaleRequest -> {
                    PriceScale scale = new PriceScale();
                    scale.setPriceCondition(priceCondition);
                    scale.setFromQuantity(scaleRequest.getQuantity());
                    scale.setScalePrice(scaleRequest.getPrice());
                    return scale;
                })
                .collect(Collectors.toList());
            
            priceScaleRepository.saveAll(scales);
        }
    }

    @Transactional
    public void update(Long id, PurchasePriceRequest request) {
        validateRequest(request);
        
        PriceCondition priceCondition = priceConditionRepository.findByIdAndPriceTypeAndDeletedFalse(id, PriceType.PURCHASE)
            .orElseThrow(() -> new ResourceNotFoundException("購買価格", id));
        
        purchasePriceMapper.updateEntityFromRequest(request, priceCondition);
        
        // 関連エンティティの更新
        if (request.getItemId() != null) {
            Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("品目", request.getItemId()));
            priceCondition.setItem(item);
            priceCondition.setItemCode(item.getItemCode());
        } else if (request.getItemCode() != null && !request.getItemCode().isEmpty()) {
            Item item = itemRepository.findByItemCode(request.getItemCode())
                .orElseThrow(() -> new ResourceNotFoundException("品目コード", request.getItemCode()));
            priceCondition.setItem(item);
            priceCondition.setItemId(item.getId());
            priceCondition.setItemCode(item.getItemCode());
        }
        
        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("仕入先", request.getSupplierId()));
            priceCondition.setSupplier(supplier);
            priceCondition.setSupplierCode(supplier.getSupplierCode());
        } else if (request.getSupplierCode() != null && !request.getSupplierCode().isEmpty()) {
            Supplier supplier = supplierRepository.findBySupplierCode(request.getSupplierCode())
                .orElseThrow(() -> new ResourceNotFoundException("仕入先コード", request.getSupplierCode()));
            priceCondition.setSupplier(supplier);
            priceCondition.setSupplierId(supplier.getId());
            priceCondition.setSupplierCode(supplier.getSupplierCode());
        }
        
        priceConditionRepository.save(priceCondition);
        
        // 既存の数量スケールを削除
        priceScaleRepository.deleteByPriceConditionId(id);
        
        // 新しい数量スケールを保存
        if (request.getPriceScales() != null && !request.getPriceScales().isEmpty()) {
            List<PriceScale> scales = request.getPriceScales().stream()
                .map(scaleRequest -> {
                    PriceScale scale = new PriceScale();
                    scale.setPriceCondition(priceCondition);
                    scale.setFromQuantity(scaleRequest.getQuantity());
                    scale.setScalePrice(scaleRequest.getPrice());
                    return scale;
                })
                .collect(Collectors.toList());
            
            priceScaleRepository.saveAll(scales);
        }
    }

    @Transactional
    public void delete(Long id) {
        PriceCondition priceCondition = priceConditionRepository.findByIdAndPriceTypeAndDeletedFalse(id, PriceType.PURCHASE)
            .orElseThrow(() -> new ResourceNotFoundException("購買価格", id));
        
        priceCondition.setDeleted(true);
        priceConditionRepository.save(priceCondition);
    }

    private void validateRequest(PurchasePriceRequest request) {
        if (request.getItemId() == null && (request.getItemCode() == null || request.getItemCode().isEmpty())) {
            throw new IllegalArgumentException("品目IDまたは品目コードは必須です。");
        }
        
        if (request.getSupplierId() == null && (request.getSupplierCode() == null || request.getSupplierCode().isEmpty())) {
            throw new IllegalArgumentException("仕入先IDまたは仕入先コードは必須です。");
        }
        
        if (request.getBasePrice() == null || request.getBasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("基本価格は0以上の値を指定してください。");
        }
        
        if (request.getValidFromDate() == null || request.getValidToDate() == null) {
            throw new IllegalArgumentException("有効期間の開始日と終了日は必須です。");
        }
        
        if (request.getValidFromDate().isAfter(request.getValidToDate())) {
            throw new IllegalArgumentException("有効開始日は有効終了日より前の日付を指定してください。");
        }
        
        // 数量スケールのバリデーション
        if (request.getPriceScales() != null) {
            for (var scale : request.getPriceScales()) {
                if (scale.getQuantity() == null || scale.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("数量スケールの数量は0より大きい値を指定してください。");
                }
                
                if (scale.getPrice() == null || scale.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("数量スケールの価格は0以上の値を指定してください。");
                }
            }
        }
    }
} 