package com.minierpapp.model.product.mapper;

import com.minierpapp.model.product.Product;
import com.minierpapp.model.product.dto.ProductRequest;
import com.minierpapp.model.product.dto.ProductResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }

        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setItemCode(product.getItemCode());
        response.setItemName(product.getItemName());
        response.setDescription(product.getDescription());
        response.setUnit(product.getUnit());
        response.setStatus(product.getStatus());
        response.setMinimumStock(product.getMinimumStock());
        response.setMaximumStock(product.getMaximumStock());
        response.setReorderPoint(product.getReorderPoint());
        response.setCreatedAt(product.getCreatedAt());
        response.setCreatedBy(product.getCreatedBy());
        response.setUpdatedAt(product.getUpdatedAt());
        response.setUpdatedBy(product.getUpdatedBy());
        return response;
    }

    public Product toEntity(ProductRequest request) {
        if (request == null) {
            return null;
        }

        Product product = new Product();
        product.setItemCode(request.getItemCode());
        product.setItemName(request.getItemName());
        product.setDescription(request.getDescription());
        product.setUnit(request.getUnit());
        product.setStatus(request.getStatus());
        product.setMinimumStock(request.getMinimumStock());
        product.setMaximumStock(request.getMaximumStock());
        product.setReorderPoint(request.getReorderPoint());
        product.setDeleted(false);
        product.setCreatedAt(LocalDateTime.now());
        return product;
    }

    public void updateEntityFromRequest(ProductRequest request, Product product) {
        if (request == null || product == null) {
            return;
        }

        product.setItemCode(request.getItemCode());
        product.setItemName(request.getItemName());
        product.setDescription(request.getDescription());
        product.setUnit(request.getUnit());
        product.setStatus(request.getStatus());
        product.setMinimumStock(request.getMinimumStock());
        product.setMaximumStock(request.getMaximumStock());
        product.setReorderPoint(request.getReorderPoint());
    }
}