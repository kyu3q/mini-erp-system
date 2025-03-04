package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.price.dto.PurchasePriceRequest;
import com.minierpapp.model.price.dto.PurchasePriceResponse;
import com.minierpapp.model.price.entity.PriceCondition;
import com.minierpapp.model.price.entity.PriceType;
import com.minierpapp.model.price.entity.PriceScale;
import com.minierpapp.model.price.entity.PurchasePrice;
import com.minierpapp.model.price.mapper.PurchasePriceMapper;
import com.minierpapp.model.supplier.Supplier;
import com.minierpapp.repository.ItemRepository;
import com.minierpapp.repository.PurchasePriceRepository;
import com.minierpapp.repository.PriceScaleRepository;
import com.minierpapp.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchasePriceService {
    private final PurchasePriceRepository purchasePriceRepository;
    private final PriceScaleRepository priceScaleRepository;
    private final ItemRepository itemRepository;
    private final SupplierRepository supplierRepository;
    private final PurchasePriceMapper purchasePriceMapper;

    @Transactional(readOnly = true)
    public List<PurchasePriceResponse> findAll() {
        return purchasePriceMapper.toResponseList(
            purchasePriceRepository.findByDeletedFalse()
        );
    }

    @Transactional(readOnly = true)
    public PurchasePriceResponse findById(Long id) {
        return purchasePriceMapper.entityToResponse(
            purchasePriceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("購買価格", id))
        );
    }

    @Transactional(readOnly = true)
    public List<PurchasePriceResponse> search(Long itemId, Long supplierId) {
        List<PurchasePrice> prices;
        
        if (itemId != null && supplierId != null) {
            prices = purchasePriceRepository.findByItemIdAndSupplierIdAndDeletedFalse(itemId, supplierId);
        } else if (itemId != null) {
            prices = purchasePriceRepository.findByItemIdAndDeletedFalse(itemId);
        } else if (supplierId != null) {
            prices = purchasePriceRepository.findBySupplierIdAndDeletedFalse(supplierId);
        } else {
            prices = purchasePriceRepository.findByDeletedFalse();
        }
        
        return purchasePriceMapper.toResponseList(prices);
    }

    @Transactional
    public void create(PurchasePriceRequest request) {
        validateRequest(request);
        
        // マッパーを使用してエンティティに変換
        PurchasePrice purchasePrice = purchasePriceMapper.requestToEntity(request);
        
        // マッピング後の値を確認
        System.out.println("Service: After mapping - itemId=" + purchasePrice.getItemId() 
            + ", supplierId=" + purchasePrice.getSupplierId());
        
        // 念のため重要なIDフィールドを明示的に設定
        purchasePrice.setItemId(request.getItemId());
        purchasePrice.setSupplierId(request.getSupplierId());
        
        // 関連エンティティの設定
        if (request.getItemId() != null) {
            Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("品目", request.getItemId()));
            purchasePrice.setItem(item);
            purchasePrice.setItemCode(item.getItemCode());
        }
        
        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("仕入先", request.getSupplierId()));
            purchasePrice.setSupplier(supplier);
            purchasePrice.setSupplierCode(supplier.getSupplierCode());
        }
        
        // 保存前の最終確認
        System.out.println("Service: Before save - itemId=" + purchasePrice.getItemId() 
            + ", supplierId=" + purchasePrice.getSupplierId());
        
        purchasePriceRepository.save(purchasePrice);
        
        // 数量スケールの保存
        if (request.getPriceScales() != null && !request.getPriceScales().isEmpty()) {
            List<PriceScale> scales = request.getPriceScales().stream()
                .map(scaleRequest -> {
                    PriceScale scale = new PriceScale();
                    scale.setPriceCondition(purchasePrice);
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
        
        PurchasePrice priceCondition = purchasePriceRepository.findByIdAndDeletedFalse(id)
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
        
        purchasePriceRepository.save(priceCondition);
        
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
        PurchasePrice priceCondition = purchasePriceRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("購買価格", id));
        
        priceCondition.setDeleted(true);
        purchasePriceRepository.save(priceCondition);
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