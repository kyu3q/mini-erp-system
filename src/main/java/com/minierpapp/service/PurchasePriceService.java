package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.price.dto.PurchasePriceRequest;
import com.minierpapp.model.price.dto.PurchasePriceResponse;
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
    public PurchasePriceResponse create(PurchasePriceRequest request) {
        System.out.println("PurchasePriceService.create開始");
        System.out.println("サービス内: itemId=" + request.getItemId() + ", itemCode=" + request.getItemCode());
        System.out.println("サービス内: supplierId=" + request.getSupplierId() + ", supplierCode=" + request.getSupplierCode());
        System.out.println("サービス内: customerId=" + request.getCustomerId() + ", customerCode=" + request.getCustomerCode());
        
        // 必須フィールドの再検証
        if (request.getItemId() == null) {
            throw new IllegalArgumentException("品目IDは必須です（サービス内）");
        }
        if (request.getSupplierId() == null) {
            throw new IllegalArgumentException("仕入先IDは必須です（サービス内）");
        }
        
        // エンティティへの変換
        PurchasePrice entity = purchasePriceMapper.requestToEntity(request);
        System.out.println("エンティティ変換後: itemId=" + entity.getItemId() + ", itemCode=" + entity.getItemCode());
        System.out.println("エンティティ変換後: supplierId=" + entity.getSupplierId() + ", supplierCode=" + entity.getSupplierCode());
        System.out.println("エンティティ変換後: customerId=" + entity.getCustomerId() + ", customerCode=" + entity.getCustomerCode());
        
        // 保存
        entity = purchasePriceRepository.save(entity);
        System.out.println("保存後: id=" + entity.getId());
        
        System.out.println("PurchasePriceService.create完了");
        return purchasePriceMapper.entityToResponse(entity);
    }

    @Transactional
    public void update(Long id, PurchasePriceRequest request) {
        validateRequest(request);
        
        PurchasePrice purchasePrice = purchasePriceRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("購買価格", id));
        
        purchasePriceMapper.updateEntityFromRequest(request, purchasePrice);
        
        // 関連エンティティの更新
        if (request.getItemId() != null) {
            Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("品目", request.getItemId()));
            purchasePrice.setItem(item);
            purchasePrice.setItemCode(item.getItemCode());
        } else if (request.getItemCode() != null && !request.getItemCode().isEmpty()) {
            Item item = itemRepository.findByItemCode(request.getItemCode())
                .orElseThrow(() -> new ResourceNotFoundException("品目コード", request.getItemCode()));
            purchasePrice.setItem(item);
            purchasePrice.setItemId(item.getId());
            purchasePrice.setItemCode(item.getItemCode());
        }
        
        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("仕入先", request.getSupplierId()));
            purchasePrice.setSupplier(supplier);
            purchasePrice.setSupplierCode(supplier.getSupplierCode());
        } else if (request.getSupplierCode() != null && !request.getSupplierCode().isEmpty()) {
            Supplier supplier = supplierRepository.findBySupplierCode(request.getSupplierCode())
                .orElseThrow(() -> new ResourceNotFoundException("仕入先コード", request.getSupplierCode()));
            purchasePrice.setSupplier(supplier);
            purchasePrice.setSupplierId(supplier.getId());
            purchasePrice.setSupplierCode(supplier.getSupplierCode());
        }
        
        purchasePriceRepository.save(purchasePrice);
        
        // 既存の数量スケールを削除
        priceScaleRepository.deleteByPriceId(id);
        
        // 新しい数量スケールを保存
        if (request.getPriceScales() != null && !request.getPriceScales().isEmpty()) {
            List<PriceScale> scales = request.getPriceScales().stream()
                .map(scaleRequest -> {
                    PriceScale scale = new PriceScale();
                    scale.setPrice(purchasePrice);
                    scale.setFromQuantity(scaleRequest.getFromQuantity());
                    scale.setToQuantity(scaleRequest.getToQuantity());
                    scale.setScalePrice(scaleRequest.getScalePrice());
                    return scale;
                })
                .collect(Collectors.toList());
            
            priceScaleRepository.saveAll(scales);
        }
    }

    @Transactional
    public void delete(Long id) {
        PurchasePrice purchasePrice = purchasePriceRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("購買価格", id));
        
        purchasePrice.setDeleted(true);
        purchasePriceRepository.save(purchasePrice);
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
                if (scale.getFromQuantity() == null || scale.getFromQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("数量スケールの開始数量は0より大きい値を指定してください。");
                }
                
                if (scale.getScalePrice() == null || scale.getScalePrice().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("数量スケールの価格は0以上の値を指定してください。");
                }
            }
        }
    }
} 