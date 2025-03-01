package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.price.dto.SalesPriceRequest;
import com.minierpapp.model.price.dto.SalesPriceResponse;
import com.minierpapp.model.price.entity.PriceCondition;
import com.minierpapp.model.price.entity.PriceType;
import com.minierpapp.model.price.entity.PriceScale;
import com.minierpapp.model.price.mapper.SalesPriceMapper;
import com.minierpapp.repository.CustomerRepository;
import com.minierpapp.repository.ItemRepository;
import com.minierpapp.repository.SalesPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SalesPriceService {
    private final SalesPriceRepository salesPriceRepository;
    private final ItemRepository itemRepository;
    private final CustomerRepository customerRepository;
    private final SalesPriceMapper salesPriceMapper;

    @Transactional(readOnly = true)
    public List<SalesPriceResponse> findAll() {
        return salesPriceMapper.toResponseList(salesPriceRepository.findAllSalesPrices());
    }

    @Transactional(readOnly = true)
    public SalesPriceResponse findById(Long id) {
        return salesPriceRepository.findSalesPriceById(id)
            .map(salesPriceMapper::entityToResponse)
            .orElseThrow(() -> new ResourceNotFoundException("販売価格", "ID", id));
    }

    @Transactional(readOnly = true)
    public List<SalesPriceResponse> search(String itemCode, String customerCode, LocalDate date) {
        return salesPriceMapper.toResponseList(
            salesPriceRepository.searchSalesPrices(itemCode, customerCode, date)
        );
    }

    @Transactional
    public SalesPriceResponse create(SalesPriceRequest request) {
        validateSalesPriceRequest(request);
        
        PriceCondition priceCondition = new PriceCondition();
        priceCondition.setPriceType(PriceType.SALES);
        
        // 基本情報の設定
        setupSalesPrice(priceCondition, request);
        
        // 保存
        priceCondition = salesPriceRepository.save(priceCondition);
        return salesPriceMapper.entityToResponse(priceCondition);
    }

    @Transactional
    public SalesPriceResponse update(Long id, SalesPriceRequest request) {
        validateSalesPriceRequest(request);
        
        PriceCondition priceCondition = salesPriceRepository.findSalesPriceById(id)
            .orElseThrow(() -> new ResourceNotFoundException("販売価格", "ID", id));
        
        // 基本情報の更新
        salesPriceMapper.updateEntityFromRequest(request, priceCondition);
        
        // 関連エンティティの更新
        updateSalesPriceRelations(priceCondition, request);
        
        // 数量スケールの更新
        updatePriceScales(priceCondition, request);
        
        // 保存
        priceCondition = salesPriceRepository.save(priceCondition);
        return salesPriceMapper.entityToResponse(priceCondition);
    }

    @Transactional
    public void delete(Long id) {
        PriceCondition priceCondition = salesPriceRepository.findSalesPriceById(id)
            .orElseThrow(() -> new ResourceNotFoundException("販売価格", "ID", id));
        
        priceCondition.setDeleted(true);
        salesPriceRepository.save(priceCondition);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculatePrice(String itemCode, String customerCode, BigDecimal quantity) {
        LocalDate today = LocalDate.now();
        Optional<PriceCondition> priceOpt = salesPriceRepository.findActiveSalesPrice(itemCode, customerCode, today);
        
        if (priceOpt.isPresent()) {
            return priceOpt.get().calculatePrice(quantity);
        }
        
        // 顧客別価格がない場合は標準価格を検索
        return salesPriceRepository.findActiveStandardSalesPrice(itemCode, today)
            .map(price -> price.calculatePrice(quantity))
            .orElseThrow(() -> new ResourceNotFoundException("販売価格", "商品コード", itemCode));
    }
    
    @Transactional(readOnly = true)
    public List<SalesPriceResponse> findExpiredPrices() {
        return salesPriceMapper.toResponseList(
            salesPriceRepository.findExpiredSalesPrices(LocalDate.now())
        );
    }
    
    @Transactional(readOnly = true)
    public List<SalesPriceResponse> findSoonExpiringPrices(int daysThreshold) {
        LocalDate today = LocalDate.now();
        LocalDate thresholdDate = today.plusDays(daysThreshold);
        return salesPriceMapper.toResponseList(
            salesPriceRepository.findSoonExpiringPrices(today, thresholdDate)
        );
    }

    // プライベートヘルパーメソッド
    private void setupSalesPrice(PriceCondition priceCondition, SalesPriceRequest request) {
        // 品目の設定
        Item item = itemRepository.findById(request.getItemId())
            .orElseThrow(() -> new ResourceNotFoundException("品目", "ID", request.getItemId()));
        priceCondition.setItem(item);
        priceCondition.setItemCode(item.getItemCode());
        
        // 顧客の設定
        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("顧客", "ID", request.getCustomerId()));
            priceCondition.setCustomer(customer);
            priceCondition.setCustomerCode(customer.getCustomerCode());
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
                scale.setFromQuantity(new BigDecimal(scaleRequest.getQuantity().toString()));
                scale.setScalePrice(scaleRequest.getPrice());
                priceCondition.addPriceScale(scale);
            });
        }
    }
    
    private void updateSalesPriceRelations(PriceCondition priceCondition, SalesPriceRequest request) {
        // 品目の更新
        if (!priceCondition.getItem().getId().equals(request.getItemId())) {
            Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("品目", "ID", request.getItemId()));
            priceCondition.setItem(item);
            priceCondition.setItemCode(item.getItemCode());
        }
        
        // 顧客の更新
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
    
    private void updatePriceScales(PriceCondition priceCondition, SalesPriceRequest request) {
        // 既存のスケールをクリア
        priceCondition.getPriceScales().clear();
        
        // 新しいスケールを追加
        if (request.getPriceScales() != null) {
            request.getPriceScales().forEach(scaleRequest -> {
                PriceScale scale = new PriceScale();
                scale.setFromQuantity(new BigDecimal(scaleRequest.getQuantity().toString()));
                scale.setScalePrice(scaleRequest.getPrice());
                priceCondition.addPriceScale(scale);
            });
        }
    }
    
    private void validateSalesPriceRequest(SalesPriceRequest request) {
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
                    throw new IllegalArgumentException("数量スケールの数量は0より大きい値を指定してください。");
                }
                
                if (scale.getPrice() == null || scale.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("数量スケールの価格は0以上の値を指定してください。");
                }
            }
        }
    }
} 