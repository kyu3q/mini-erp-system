package com.minierpapp.service;

import com.minierpapp.model.product.Product;
import com.minierpapp.model.product.dto.ProductRequest;
import com.minierpapp.model.product.mapper.ProductMapper;
import com.minierpapp.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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
        if (productRepository.existsByProductCodeAndDeletedFalse(request.getProductCode())) {
            throw new IllegalArgumentException("Product code already exists: " + request.getProductCode());
        }
        Product product = productMapper.toEntity(request);
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, ProductRequest request) {
        Product existingProduct = findById(id);
        
        // 商品コードが変更されており、かつ新しい商品コードが既に存在する場合はエラー
        if (!existingProduct.getProductCode().equals(request.getProductCode()) &&
            productRepository.existsByProductCodeAndDeletedFalse(request.getProductCode())) {
            throw new IllegalArgumentException("Product code already exists: " + request.getProductCode());
        }

        productMapper.updateEntityFromRequest(request, existingProduct);
        return productRepository.save(existingProduct);
    }

    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        product.setDeleted(true);
        productRepository.save(product);
    }
}