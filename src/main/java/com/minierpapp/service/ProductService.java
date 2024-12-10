package com.minierpapp.service;

import com.minierpapp.model.product.Product;
import com.minierpapp.model.product.dto.ProductRequest;
import com.minierpapp.model.product.mapper.ProductMapper;
import com.minierpapp.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findByDeletedFalse();
    }

    @Transactional(readOnly = true)
    public List<Product> search(String productCode, String productName) {
        productCode = productCode != null ? productCode.trim() : "";
        productName = productName != null ? productName.trim() : "";
        return productRepository.findByProductCodeContainingAndProductNameContainingAndDeletedFalse(productCode, productName);
    }

    @Transactional(readOnly = true)
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findByDeletedFalse(pageable);
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Product findByProductCode(String productCode) {
        return productRepository.findByProductCodeAndDeletedFalse(productCode)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with code: " + productCode));
    }

    @Transactional
    public Product create(ProductRequest request) {
        // 商品コードの重複チェック
        if (productRepository.existsByProductCodeAndDeletedFalse(request.getProductCode())) {
            throw new IllegalArgumentException("商品コード「" + request.getProductCode() + "」は既に使用されています");
        }

        // 在庫レベルのバリデーション
        validateStockLevels(request);

        // 新規商品の作成
        Product product = productMapper.toEntity(request);
        product.setCreatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, ProductRequest request) {
        Product existingProduct = findById(id);

        // 商品コードの変更は不可
        if (!existingProduct.getProductCode().equals(request.getProductCode())) {
            throw new IllegalArgumentException("商品コードは変更できません");
        }

        // 在庫レベルのバリデーション
        validateStockLevels(request);

        // 商品情報の更新
        existingProduct.setProductName(request.getProductName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setUnit(request.getUnit());
        existingProduct.setStatus(request.getStatus());
        existingProduct.setMinimumStock(request.getMinimumStock());
        existingProduct.setMaximumStock(request.getMaximumStock());
        existingProduct.setReorderPoint(request.getReorderPoint());
        existingProduct.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(existingProduct);
    }

    private void validateStockLevels(ProductRequest request) {
        Integer minimumStock = request.getMinimumStock();
        Integer maximumStock = request.getMaximumStock();
        Integer reorderPoint = request.getReorderPoint();

        if (minimumStock != null && maximumStock != null && minimumStock > maximumStock) {
            throw new IllegalArgumentException("最小在庫数は最大在庫数以下である必要があります");
        }

        if (reorderPoint != null) {
            if (minimumStock != null && reorderPoint < minimumStock) {
                throw new IllegalArgumentException("発注点は最小在庫数以上である必要があります");
            }
            if (maximumStock != null && reorderPoint > maximumStock) {
                throw new IllegalArgumentException("発注点は最大在庫数以下である必要があります");
            }
        }
    }

    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        product.setDeleted(true);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }
}