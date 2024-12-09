package com.minierpapp.model.product.mapper;

import com.minierpapp.model.product.Product;
import com.minierpapp.model.product.dto.ProductRequest;
import com.minierpapp.model.product.dto.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }

        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setProductCode(product.getProductCode());
        response.setProductName(product.getProductName());
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
        product.setProductCode(request.getProductCode());
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setUnit(request.getUnit());
        product.setStatus(request.getStatus());
        product.setMinimumStock(request.getMinimumStock());
        product.setMaximumStock(request.getMaximumStock());
        product.setReorderPoint(request.getReorderPoint());
        return product;
    }

    public void updateEntityFromRequest(ProductRequest request, Product product) {
        if (request == null || product == null) {
            return;
        }

        product.setProductCode(request.getProductCode());
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setUnit(request.getUnit());
        product.setStatus(request.getStatus());
        product.setMinimumStock(request.getMinimumStock());
        product.setMaximumStock(request.getMaximumStock());
        product.setReorderPoint(request.getReorderPoint());
    }
}