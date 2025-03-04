package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.price.dto.SalesPriceRequest;
import com.minierpapp.model.price.dto.SalesPriceResponse;
import com.minierpapp.model.price.entity.PriceScale;
import com.minierpapp.model.price.entity.SalesPrice;
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
        return salesPriceMapper.toResponseList(salesPriceRepository.findByDeletedFalse());
    }

    @Transactional(readOnly = true)
    public SalesPriceResponse findById(Long id) {
        return salesPriceRepository.findByIdAndDeletedFalse(id)
            .map(salesPriceMapper::entityToResponse)
            .orElseThrow(() -> new ResourceNotFoundException("販売価格", "ID", id));
    }

    @Transactional(readOnly = true)
    public List<SalesPriceResponse> search(String itemCode, String customerCode, LocalDate date) {
        // 検索条件に応じたクエリを実行
        List<SalesPrice> results;
        if (date != null) {
            // 日付指定がある場合は有効期間内の価格を検索
            results = salesPriceRepository.findByItemCodeContainingAndCustomerCodeContainingAndValidFromDateLessThanEqualAndValidToDateGreaterThanEqualAndDeletedFalse(
                itemCode != null ? itemCode : "", 
                customerCode != null ? customerCode : "", 
                date, date);
        } else {
            // 日付指定がない場合は全ての価格を検索
            results = salesPriceRepository.findByItemCodeContainingAndCustomerCodeContainingAndDeletedFalse(
                itemCode != null ? itemCode : "", 
                customerCode != null ? customerCode : "");
        }
        return salesPriceMapper.toResponseList(results);
    }

    @Transactional
    public SalesPriceResponse create(SalesPriceRequest request) {
        validateSalesPriceRequest(request);
        
        SalesPrice salesPrice = new SalesPrice();
        
        // 基本情報の設定
        setupSalesPrice(salesPrice, request);
        
        // 保存
        salesPrice = salesPriceRepository.save(salesPrice);
        return salesPriceMapper.entityToResponse(salesPrice);
    }

    @Transactional
    public SalesPriceResponse update(Long id, SalesPriceRequest request) {
        validateSalesPriceRequest(request);
        
        SalesPrice salesPrice = salesPriceRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("販売価格", "ID", id));
        
        // 基本情報の更新
        salesPriceMapper.updateEntityFromRequest(request, salesPrice);
        
        // 関連エンティティの更新
        updateSalesPriceRelations(salesPrice, request);
        
        // 数量スケールの更新
        updatePriceScales(salesPrice, request);
        
        // 保存
        salesPrice = salesPriceRepository.save(salesPrice);
        return salesPriceMapper.entityToResponse(salesPrice);
    }

    @Transactional
    public void delete(Long id) {
        SalesPrice salesPrice = salesPriceRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("販売価格", "ID", id));
        
        salesPrice.setDeleted(true);
        salesPriceRepository.save(salesPrice);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculatePrice(String itemCode, String customerCode, BigDecimal quantity) {
        LocalDate today = LocalDate.now();
        
        // 顧客別価格を検索
        Optional<SalesPrice> priceOpt = salesPriceRepository.findByItemCodeAndCustomerCodeAndValidFromDateLessThanEqualAndValidToDateGreaterThanEqualAndStatusAndDeletedFalse(
            itemCode, customerCode, today, today, "ACTIVE");
        
        if (priceOpt.isPresent()) {
            return calculatePriceWithScales(priceOpt.get(), quantity);
        }
        
        // 顧客別価格がない場合は標準価格を検索
        return salesPriceRepository.findByItemCodeAndCustomerCodeIsNullAndValidFromDateLessThanEqualAndValidToDateGreaterThanEqualAndStatusAndDeletedFalse(
                itemCode, today, today, "ACTIVE")
            .map(price -> calculatePriceWithScales(price, quantity))
            .orElseThrow(() -> new ResourceNotFoundException("販売価格", "商品コード", itemCode));
    }
    
    private BigDecimal calculatePriceWithScales(SalesPrice price, BigDecimal quantity) {
        // 数量スケールに基づいて価格を計算
        for (PriceScale scale : price.getPriceScales()) {
            if (quantity.compareTo(scale.getFromQuantity()) >= 0 && 
                (scale.getToQuantity() == null || quantity.compareTo(scale.getToQuantity()) <= 0)) {
                return scale.getScalePrice();
            }
        }
        // スケールに該当しない場合は基本価格を返す
        return price.getBasePrice();
    }
    
    @Transactional(readOnly = true)
    public List<SalesPriceResponse> findExpiredPrices() {
        return salesPriceMapper.toResponseList(
            salesPriceRepository.findByValidToDateLessThanAndDeletedFalse(LocalDate.now())
        );
    }
    
    @Transactional(readOnly = true)
    public List<SalesPriceResponse> findSoonExpiringPrices(int daysThreshold) {
        LocalDate today = LocalDate.now();
        LocalDate thresholdDate = today.plusDays(daysThreshold);
        return salesPriceMapper.toResponseList(
            salesPriceRepository.findByValidToDateGreaterThanEqualAndValidToDateLessThanEqualAndDeletedFalse(today, thresholdDate)
        );
    }

    // プライベートヘルパーメソッド
    private void setupSalesPrice(SalesPrice salesPrice, SalesPriceRequest request) {
        // 品目の設定
        Item item = itemRepository.findById(request.getItemId())
            .orElseThrow(() -> new ResourceNotFoundException("品目", "ID", request.getItemId()));
        salesPrice.setItem(item);
        salesPrice.setItemCode(item.getItemCode());
        
        // 顧客の設定
        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("顧客", "ID", request.getCustomerId()));
            salesPrice.setCustomer(customer);
            salesPrice.setCustomerCode(customer.getCustomerCode());
        }
        
        // 基本情報の設定
        salesPrice.setBasePrice(request.getBasePrice());
        salesPrice.setCurrencyCode(request.getCurrencyCode());
        salesPrice.setValidFromDate(request.getValidFromDate());
        salesPrice.setValidToDate(request.getValidToDate());
        salesPrice.setStatus(request.getStatus());
        
        // 数量スケールの設定
        if (request.getPriceScales() != null) {
            request.getPriceScales().forEach(scaleRequest -> {
                PriceScale scale = new PriceScale();
                scale.setFromQuantity(new BigDecimal(scaleRequest.getQuantity().toString()));
                scale.setScalePrice(scaleRequest.getPrice());
                salesPrice.addPriceScale(scale);
            });
        }
    }
    
    private void updateSalesPriceRelations(SalesPrice salesPrice, SalesPriceRequest request) {
        // 品目の更新
        if (!salesPrice.getItem().getId().equals(request.getItemId())) {
            Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("品目", "ID", request.getItemId()));
            salesPrice.setItem(item);
            salesPrice.setItemCode(item.getItemCode());
        }
        
        // 顧客の更新
        if (request.getCustomerId() == null) {
            salesPrice.setCustomer(null);
            salesPrice.setCustomerCode(null);
        } else if (salesPrice.getCustomer() == null || 
                  !salesPrice.getCustomer().getId().equals(request.getCustomerId())) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("顧客", "ID", request.getCustomerId()));
            salesPrice.setCustomer(customer);
            salesPrice.setCustomerCode(customer.getCustomerCode());
        }
    }
    
    private void updatePriceScales(SalesPrice salesPrice, SalesPriceRequest request) {
        // 既存のスケールをクリア
        salesPrice.getPriceScales().clear();
        
        // 新しいスケールを追加
        if (request.getPriceScales() != null) {
            request.getPriceScales().forEach(scaleRequest -> {
                PriceScale scale = new PriceScale();
                scale.setFromQuantity(new BigDecimal(scaleRequest.getQuantity().toString()));
                scale.setScalePrice(scaleRequest.getPrice());
                salesPrice.addPriceScale(scale);
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