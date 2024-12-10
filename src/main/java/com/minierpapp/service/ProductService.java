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
        validateProductCode(null, request.getProductCode());
        validateStockLevels(request);
        Product product = productMapper.toEntity(request);
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, ProductRequest request) {
        Product existingProduct = findById(id);
        
        // 商品コードが変更される場合は、既存の商品を論理削除して新しい商品を作成
        if (!existingProduct.getProductCode().equals(request.getProductCode())) {
            existingProduct.setDeleted(true);
            productRepository.save(existingProduct);
            
            // 新しい商品を作成
            Product newProduct = productMapper.toEntity(request);
            validateProductCode(null, request.getProductCode());
            validateStockLevels(request);
            return productRepository.save(newProduct);
        }
        
        // 商品コードが変更されない場合は通常の更新
        validateStockLevels(request);
        productMapper.updateEntityFromRequest(request, existingProduct);
        return productRepository.save(existingProduct);
    }

    private void validateProductCode(Long productId, String productCode) {
        if (productId == null) {
            // 新規作成時
            if (productRepository.existsByProductCodeAndDeletedFalse(productCode)) {
                throw new IllegalArgumentException("商品コード「" + productCode + "」は既に使用されています");
            }
        } else {
            // 更新時：同じ商品コードを持つ非削除の商品が存在し、かつそれが異なる商品の場合はエラー
            productRepository.findByProductCodeAndDeletedFalse(productCode)
                    .ifPresent(existingProduct -> {
                        if (!existingProduct.getId().equals(productId)) {
                            throw new IllegalArgumentException("商品コード「" + productCode + "」は既に使用されています");
                        }
                    });
        }
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
        productRepository.save(product);
    }
}