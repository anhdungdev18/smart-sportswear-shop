package com.dunghaiquyen.ecommerce.modules.product.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.common.exception.ResourceNotFoundException;
import com.dunghaiquyen.ecommerce.config.CacheConfig;
import com.dunghaiquyen.ecommerce.modules.product.dto.ProductVariantResponse;
import com.dunghaiquyen.ecommerce.modules.product.dto.VariantCreateRequest;
import com.dunghaiquyen.ecommerce.modules.product.dto.VariantUpdateRequest;
import com.dunghaiquyen.ecommerce.modules.product.entity.Product;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductVariant;
import com.dunghaiquyen.ecommerce.modules.product.entity.VariantStatus;
import com.dunghaiquyen.ecommerce.modules.product.mapper.ProductMapper;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductRepository;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductVariantRepository;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stock here is create-time-initial-state only - see VariantCreateRequest /
 * VariantUpdateRequest javadoc. Nothing in this service ever writes
 * stockQuantity/reservedQuantity on an EXISTING variant; that is the
 * inventory module's job (not built in Phase D).
 */
@Service
public class ProductVariantService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductMapper productMapper;

    public ProductVariantService(
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.REPORT_OVERVIEW, allEntries = true),
            @CacheEvict(value = CacheConfig.REPORT_INVENTORY, allEntries = true)
    })
    public ProductVariantResponse create(UUID productId, VariantCreateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (variantRepository.existsBySku(request.sku())) {
            throw new BusinessRuleException("SKU already exists: " + request.sku());
        }

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(request.sku());
        variant.setSize(request.size());
        variant.setColor(request.color());
        variant.setPrice(request.price());
        variant.setCompareAtPrice(request.compareAtPrice());
        variant.setStockQuantity(request.stockQuantity() != null ? request.stockQuantity() : 0);
        variant.setStatus(request.status() != null ? request.status() : VariantStatus.ACTIVE);

        try {
            variant = variantRepository.save(variant);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessRuleException("SKU already exists: " + request.sku());
        }
        return productMapper.toVariantResponse(variant);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.REPORT_OVERVIEW, allEntries = true),
            @CacheEvict(value = CacheConfig.REPORT_INVENTORY, allEntries = true)
    })
    public ProductVariantResponse update(UUID variantId, VariantUpdateRequest request) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));

        if (request.size() != null) {
            variant.setSize(request.size());
        }
        if (request.color() != null) {
            variant.setColor(request.color());
        }
        if (request.price() != null) {
            variant.setPrice(request.price());
        }
        if (request.compareAtPrice() != null) {
            variant.setCompareAtPrice(request.compareAtPrice());
        }
        if (request.status() != null) {
            variant.setStatus(request.status());
        }

        variant = variantRepository.save(variant);
        return productMapper.toVariantResponse(variant);
    }
}
