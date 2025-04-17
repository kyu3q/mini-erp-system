package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.price.dto.PriceScaleRequest;
import com.minierpapp.model.price.dto.PurchasePriceRequest;
import com.minierpapp.model.price.dto.PurchasePriceResponse;
import com.minierpapp.model.price.entity.PriceScale;
import com.minierpapp.model.price.entity.PurchasePrice;
import com.minierpapp.model.price.mapper.PurchasePriceMapper;
import com.minierpapp.model.supplier.Supplier;
import com.minierpapp.repository.CustomerRepository;
import com.minierpapp.repository.ItemRepository;
import com.minierpapp.repository.PurchasePriceRepository;
import com.minierpapp.repository.PriceScaleRepository;
import com.minierpapp.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchasePriceService {
    private final PurchasePriceRepository purchasePriceRepository;
    private final PriceScaleRepository priceScaleRepository;
    private final ItemRepository itemRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerRepository customerRepository;
    private final PurchasePriceMapper purchasePriceMapper;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<PurchasePriceResponse> findAll() {
        List<PurchasePrice> entities = purchasePriceRepository.findAll();
        
        // スケール情報のデバッグ出力
        entities.forEach(entity -> {
            System.out.println("Price ID: " + entity.getId());
            // スケール情報を直接取得
            List<PriceScale> scales = priceScaleRepository.findByPriceIdAndDeletedFalse(entity.getId());
            entity.setPriceScales(scales);
            System.out.println("Scale count: " + scales.size());
            entity.getPriceScales().forEach(scale -> {
                System.out.println("  Scale: " + scale.getFromQuantity() + 
                    " - " + scale.getToQuantity() + " = " + scale.getScalePrice());
            });
        });
        
        List<PurchasePriceResponse> responses = purchasePriceMapper.toResponseList(entities);
        
        // レスポンスのスケール情報も確認
        responses.forEach(response -> {
            System.out.println("Response ID: " + response.getId());
            System.out.println("Response Scale count: " + 
                (response.getPriceScales() != null ? response.getPriceScales().size() : "null"));
        });
        
        // 関連エンティティの名前を設定
        for (PurchasePriceResponse response : responses) {
            setRelatedNames(response);
        }
        
        return responses;
    }

    @Transactional(readOnly = true)
    public PurchasePriceResponse findById(Long id) {
        PurchasePrice entity = purchasePriceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("購買単価が見つかりません: " + id));
        
        // スケール情報を直接取得して設定
        List<PriceScale> scales = priceScaleRepository.findByPriceIdAndDeletedFalse(entity.getId());
        entity.setPriceScales(scales);
        
        PurchasePriceResponse response = purchasePriceMapper.entityToResponse(entity);
        setRelatedNames(response);
        
        return response;
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
        // エンティティへの変換
        final PurchasePrice entity = purchasePriceMapper.requestToEntity(request);
        
        // 関連エンティティの設定
        if (request.getItemId() != null) {
            Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("品目", request.getItemId()));
            entity.setItem(item);
            entity.setItemCode(item.getItemCode());
        }
        
        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("仕入先", request.getSupplierId()));
            entity.setSupplier(supplier);
            entity.setSupplierCode(supplier.getSupplierCode());
        }
        
        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("得意先", request.getCustomerId()));
            entity.setCustomer(customer);
            entity.setCustomerCode(customer.getCustomerCode());
        }
        
        // 先に価格エンティティを保存
        final PurchasePrice savedEntity = purchasePriceRepository.save(entity);
        
        // スケール価格の設定
        savePriceScales(request.getPriceScales(), savedEntity);
        
        return purchasePriceMapper.entityToResponse(savedEntity);
    }

    @Transactional
    public PurchasePriceResponse update(Long id, PurchasePriceRequest request) {
        validateRequest(request);
        
        PurchasePrice purchasePrice = purchasePriceRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("購買価格", id));
        
        // 基本情報の更新
        purchasePriceMapper.updateEntityFromRequest(request, purchasePrice);
        
        // 関連エンティティの設定
        if (request.getItemId() != null) {
            Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("品目", request.getItemId()));
            purchasePrice.setItem(item);
            purchasePrice.setItemCode(item.getItemCode());
        }
        
        // 仕入先情報の設定
        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("仕入先", request.getSupplierId()));
            purchasePrice.setSupplier(supplier);
            purchasePrice.setSupplierCode(supplier.getSupplierCode());
        }
        
        // 得意先情報の設定（必要な場合）
        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("得意先", request.getCustomerId()));
            purchasePrice.setCustomer(customer);
            purchasePrice.setCustomerCode(customer.getCustomerCode());
        }
        
        // 既存のスケール情報を物理削除する
        deleteExistingPriceScales(id);
        
        // 購買単価を保存
        PurchasePrice savedPrice = purchasePriceRepository.save(purchasePrice);
        
        // 新しいスケール情報を保存 - 新しいトランザクションで実行
        if (request.getPriceScales() != null && !request.getPriceScales().isEmpty()) {
            createNewPriceScales(request.getPriceScales(), savedPrice);
        }
        
        // 更新後のエンティティを再取得
        PurchasePrice updatedPrice = purchasePriceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("購買価格", id));
        
        // スケール情報を明示的に取得
        List<PriceScale> updatedScales = priceScaleRepository.findByPriceIdAndDeletedFalse(id);
        updatedPrice.setPriceScales(updatedScales);
        
        // レスポンスを返す
        PurchasePriceResponse response = purchasePriceMapper.entityToResponse(updatedPrice);
        setRelatedNames(response);
        return response;
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

    private void setRelatedNames(PurchasePriceResponse response) {
        // 品目名の設定
        if (response.getItemId() != null) {
            try {
                Item item = itemRepository.findById(response.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("品目", response.getItemId()));
                if (item != null) {
                    response.setItemName(item.getItemName());
                }
            } catch (Exception e) {
                // エラーが発生しても処理を続行
                System.out.println("品目名の取得に失敗: " + e.getMessage());
            }
        }
        
        // 仕入先名の設定
        if (response.getSupplierId() != null) {
            try {
                Supplier supplier = supplierRepository.findById(response.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("仕入先", response.getSupplierId()));
                if (supplier != null) {
                    response.setSupplierName(supplier.getName());
                }
            } catch (Exception e) {
                // エラーが発生しても処理を続行
                System.out.println("仕入先名の取得に失敗: " + e.getMessage());
            }
        }

        // 得意先名の設定
        if (response.getCustomerId() != null) {
            try {
                Customer customer = customerRepository.findById(response.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("得意先", response.getCustomerId()));
                if (customer != null) {
                    response.setCustomerName(customer.getName());
                }
            } catch (Exception e) {
                // エラーが発生しても処理を続行
                System.out.println("得意先名の取得に失敗: " + e.getMessage());
            }
        }
    }

    private void savePriceScales(List<PriceScaleRequest> scales, PurchasePrice price) {
        if (scales != null && !scales.isEmpty()) {
            scales.forEach(scale -> {
                PriceScale priceScale = new PriceScale();
                priceScale.setFromQuantity(scale.getFromQuantity());
                priceScale.setToQuantity(scale.getToQuantity());
                priceScale.setScalePrice(scale.getScalePrice());
                priceScale.setPrice(price);
                price.getPriceScales().add(priceScale);
                priceScaleRepository.save(priceScale);
            });
        }
    }

    // 既存のスケール情報を物理削除するメソッド
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteExistingPriceScales(Long priceId) {
        // ネイティブSQLを使用して物理削除を実行
        priceScaleRepository.deleteByPriceIdNative(priceId);
        
        // 念のため、エンティティマネージャのキャッシュをクリア
        entityManager.flush();
        entityManager.clear();
    }

    // 新しいスケール情報を保存するメソッド
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createNewPriceScales(List<PriceScaleRequest> scaleRequests, PurchasePrice price) {
        for (PriceScaleRequest scaleRequest : scaleRequests) {
            PriceScale scale = new PriceScale();
            scale.setPrice(price);
            scale.setFromQuantity(scaleRequest.getFromQuantity());
            scale.setToQuantity(scaleRequest.getToQuantity());
            scale.setScalePrice(scaleRequest.getScalePrice());
            priceScaleRepository.save(scale);
        }
    }
} 