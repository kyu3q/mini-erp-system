package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.price.dto.PriceConditionDto;
import com.minierpapp.model.price.dto.PriceConditionRequest;
import com.minierpapp.model.price.dto.PriceConditionResponse;
import com.minierpapp.model.price.entity.PriceCondition;
import com.minierpapp.model.price.entity.PriceType;
import com.minierpapp.model.price.entity.PriceScale;
import com.minierpapp.model.price.mapper.PriceConditionMapper;
import com.minierpapp.model.supplier.Supplier;
import com.minierpapp.repository.CustomerRepository;
import com.minierpapp.repository.ItemRepository;
import com.minierpapp.repository.PriceConditionRepository;
import com.minierpapp.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PriceService {
    private final PriceConditionRepository priceConditionRepository;
    private final ItemRepository itemRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final PriceConditionMapper priceConditionMapper;

    @Transactional(readOnly = true)
    public List<PriceConditionResponse> findAllSalesPrices() {
        return priceConditionMapper.toResponseList(
            priceConditionRepository.findByPriceTypeAndDeletedFalse(PriceType.SALES)
        );
    }

    @Transactional(readOnly = true)
    public List<PriceConditionResponse> findAllPurchasePrices() {
        return priceConditionMapper.toResponseList(
            priceConditionRepository.findByPriceTypeAndDeletedFalse(PriceType.PURCHASE)
        );
    }

    @Transactional(readOnly = true)
    public PriceConditionDto findById(Long id) {
        return priceConditionRepository.findByIdAndDeletedFalse(id)
            .map(priceConditionMapper::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("価格条件", "ID", id));
    }

    @Transactional(readOnly = true)
    public List<PriceConditionResponse> searchSalesPrices(String itemCode, String customerCode, LocalDate date) {
        return priceConditionMapper.toResponseList(
            priceConditionRepository.findActiveSalesPrices(itemCode, customerCode, date)
        );
    }

    @Transactional(readOnly = true)
    public List<PriceConditionResponse> searchPurchasePrices(String itemCode, String supplierCode, LocalDate date) {
        return priceConditionMapper.toResponseList(
            priceConditionRepository.findActivePurchasePrices(itemCode, supplierCode, date)
        );
    }

    @Transactional
    public PriceConditionResponse createSalesPrice(PriceConditionRequest request) {
        validateSalesPriceRequest(request);
        PriceCondition priceCondition = new PriceCondition();
        priceCondition.setPriceType(PriceType.SALES);
        
        // 基本情報の設定
        setupPriceCondition(priceCondition, request);
        
        // 保存
        priceCondition = priceConditionRepository.save(priceCondition);
        return priceConditionMapper.entityToResponse(priceCondition);
    }

    @Transactional
    public PriceConditionResponse createPurchasePrice(PriceConditionRequest request) {
        validatePurchasePriceRequest(request);
        PriceCondition priceCondition = new PriceCondition();
        priceCondition.setPriceType(PriceType.PURCHASE);
        
        // 基本情報の設定
        setupPriceCondition(priceCondition, request);
        
        // 保存
        priceCondition = priceConditionRepository.save(priceCondition);
        return priceConditionMapper.entityToResponse(priceCondition);
    }

    @Transactional
    public PriceConditionResponse updatePrice(Long id, PriceConditionRequest request) {
        PriceCondition priceCondition = priceConditionRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("価格条件", "ID", id));
        
        if (priceCondition.isPurchasePrice()) {
            validatePurchasePriceRequest(request);
        } else {
            validateSalesPriceRequest(request);
        }
        
        // 基本情報の更新
        priceConditionMapper.updateEntityFromRequest(request, priceCondition);
        
        // 関連エンティティの更新
        updatePriceConditionRelations(priceCondition, request);
        
        // 数量スケールの更新
        updatePriceScales(priceCondition, request);
        
        // 保存
        priceCondition = priceConditionRepository.save(priceCondition);
        return priceConditionMapper.entityToResponse(priceCondition);
    }

    @Transactional
    public void deletePrice(Long id) {
        PriceCondition priceCondition = priceConditionRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("価格条件", "ID", id));
        
        priceCondition.setDeleted(true);
        priceConditionRepository.save(priceCondition);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateSalesPrice(String itemCode, String customerCode, BigDecimal quantity) {
        LocalDate today = LocalDate.now();
        Optional<PriceCondition> priceOpt = priceConditionRepository.findActiveSalesPrice(itemCode, customerCode, today);
        
        if (priceOpt.isPresent()) {
            return priceOpt.get().calculatePrice(quantity);
        }
        
        // 顧客別価格がない場合は標準価格を検索
        return priceConditionRepository.findActiveStandardSalesPrice(itemCode, today)
            .map(price -> price.calculatePrice(quantity))
            .orElseThrow(() -> new ResourceNotFoundException("販売価格", "商品コード", itemCode));
    }

    @Transactional(readOnly = true)
    public BigDecimal calculatePurchasePrice(String itemCode, String supplierCode, BigDecimal quantity) {
        LocalDate today = LocalDate.now();
        Optional<PriceCondition> priceOpt = priceConditionRepository.findActivePurchasePrice(itemCode, supplierCode, today);
        
        if (priceOpt.isPresent()) {
            return priceOpt.get().calculatePrice(quantity);
        }
        
        // 仕入先別価格がない場合は標準価格を検索
        return priceConditionRepository.findActiveStandardPurchasePrice(itemCode, today)
            .map(price -> price.calculatePrice(quantity))
            .orElseThrow(() -> new ResourceNotFoundException("仕入価格", "商品コード", itemCode));
    }

    @Transactional
    public PriceCondition save(PriceCondition priceCondition) {
        return priceConditionRepository.save(priceCondition);
    }

    // プライベートヘルパーメソッド
    
    private void setupPriceCondition(PriceCondition priceCondition, PriceConditionRequest request) {
        // 品目の設定
        Item item = itemRepository.findById(request.getItemId())
            .orElseThrow(() -> new ResourceNotFoundException("品目", "ID", request.getItemId()));
        priceCondition.setItem(item);
        priceCondition.setItemCode(item.getItemCode());
        
        // 顧客/仕入先の設定
        if (priceCondition.isPurchasePrice()) {
            if (request.getSupplierId() != null) {
                Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("仕入先", "ID", request.getSupplierId()));
                priceCondition.setSupplier(supplier);
                priceCondition.setSupplierCode(supplier.getSupplierCode());
            }
        } else {
            if (request.getCustomerId() != null) {
                Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("顧客", "ID", request.getCustomerId()));
                priceCondition.setCustomer(customer);
                priceCondition.setCustomerCode(customer.getCustomerCode());
            }
        }
        
        // 基本情報の設定
        priceCondition.setBasePrice(request.getBasePrice());
        priceCondition.setCurrencyCode(request.getCurrencyCode());
        priceCondition.setValidFromDate(request.getValidFromDate());
        priceCondition.setValidToDate(request.getValidToDate());
        priceCondition.setStatus(request.getStatus());
        
        // 数量スケールの設定
        if (request.getPriceScales() != null) {
            request.getPriceScales().forEach(scaleRequest -> {
                PriceScale scale = new PriceScale();
                scale.setFromQuantity(scaleRequest.getQuantity());
                scale.setToQuantity(null);
                scale.setScalePrice(scaleRequest.getPrice());
                priceCondition.addPriceScale(scale);
            });
        }
    }
    
    private void updatePriceConditionRelations(PriceCondition priceCondition, PriceConditionRequest request) {
        // 品目の更新
        if (!priceCondition.getItem().getId().equals(request.getItemId())) {
            Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("品目", "ID", request.getItemId()));
            priceCondition.setItem(item);
            priceCondition.setItemCode(item.getItemCode());
        }
        
        // 顧客/仕入先の更新
        if (priceCondition.isPurchasePrice()) {
            if (request.getSupplierId() == null) {
                priceCondition.setSupplier(null);
                priceCondition.setSupplierCode(null);
            } else if (priceCondition.getSupplier() == null || 
                      !priceCondition.getSupplier().getId().equals(request.getSupplierId())) {
                Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("仕入先", "ID", request.getSupplierId()));
                priceCondition.setSupplier(supplier);
                priceCondition.setSupplierCode(supplier.getSupplierCode());
            }
        } else {
            if (request.getCustomerId() == null) {
                priceCondition.setCustomer(null);
                priceCondition.setCustomerCode(null);
            } else if (priceCondition.getCustomer() == null || 
                      !priceCondition.getCustomer().getId().equals(request.getCustomerId())) {
                Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("顧客", "ID", request.getCustomerId()));
                priceCondition.setCustomer(customer);
                priceCondition.setCustomerCode(customer.getCustomerCode());
            }
        }
    }
    
    private void updatePriceScales(PriceCondition priceCondition, PriceConditionRequest request) {
        // 既存のスケールをクリア
        priceCondition.getPriceScales().clear();
        
        // 新しいスケールを追加
        if (request.getPriceScales() != null) {
            request.getPriceScales().forEach(scaleRequest -> {
                PriceScale scale = new PriceScale();
                scale.setFromQuantity(scaleRequest.getQuantity());
                scale.setToQuantity(null);
                scale.setScalePrice(scaleRequest.getPrice());
                priceCondition.addPriceScale(scale);
            });
        }
    }
    
    private void validateSalesPriceRequest(PriceConditionRequest request) {
        if (request.getItemId() == null) {
            throw new IllegalArgumentException("品目IDは必須です。");
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
                    throw new IllegalArgumentException("数量スケールの開始数量は0より大きい値を指定してください。");
                }
                
                if (scale.getPrice() == null || scale.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("数量スケールの価格は0以上の値を指定してください。");
                }
            }
        }
    }
    
    private void validatePurchasePriceRequest(PriceConditionRequest request) {
        if (request.getItemId() == null) {
            throw new IllegalArgumentException("品目IDは必須です。");
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
                    throw new IllegalArgumentException("数量スケールの開始数量は0より大きい値を指定してください。");
                }
                
                if (scale.getPrice() == null || scale.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("数量スケールの価格は0以上の値を指定してください。");
                }
            }
        }
    }
}