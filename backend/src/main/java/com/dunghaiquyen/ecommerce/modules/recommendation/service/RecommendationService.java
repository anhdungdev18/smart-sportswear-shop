package com.dunghaiquyen.ecommerce.modules.recommendation.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.modules.brand.entity.Brand;
import com.dunghaiquyen.ecommerce.modules.category.entity.Category;
import com.dunghaiquyen.ecommerce.modules.product.dto.CatalogRefResponse;
import com.dunghaiquyen.ecommerce.modules.product.dto.ProductListItemResponse;
import com.dunghaiquyen.ecommerce.modules.product.entity.Product;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductImage;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductVariant;
import com.dunghaiquyen.ecommerce.modules.product.entity.VariantStatus;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductImageRepository;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductVariantRepository;
import com.dunghaiquyen.ecommerce.modules.product.util.ThumbnailResolver;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RecommendationItemResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RecommendationResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.AssociationRule;
import com.dunghaiquyen.ecommerce.modules.recommendation.repository.AssociationRuleRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationService {

    private static final int DEFAULT_LIMIT = 8;
    private static final int MAX_LIMIT = 20;
    private static final String FREQUENTLY_BOUGHT_TOGETHER = "FREQUENTLY_BOUGHT_TOGETHER";

    private final AssociationRuleRepository associationRuleRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;

    public RecommendationService(
            AssociationRuleRepository associationRuleRepository,
            ProductVariantRepository variantRepository,
            ProductImageRepository imageRepository) {
        this.associationRuleRepository = associationRuleRepository;
        this.variantRepository = variantRepository;
        this.imageRepository = imageRepository;
    }

    @Transactional(readOnly = true)
    public RecommendationResponse getFrequentlyBoughtTogether(UUID productId, Integer limit) {
        int resolvedLimit = resolveLimit(limit);

        List<AssociationRule> rules = associationRuleRepository.findActiveRulesByAntecedentProductId(
                productId,
                PageRequest.of(0, resolvedLimit)
        );

        if (rules.isEmpty()) {
            return new RecommendationResponse(productId, FREQUENTLY_BOUGHT_TOGETHER, List.of());
        }

        List<UUID> recommendedProductIds = rules.stream()
                .map(rule -> rule.getConsequentProduct().getId())
                .toList();

        Map<UUID, List<ProductVariant>> variantsByProduct =
                variantRepository.findAllByProductIdIn(recommendedProductIds)
                        .stream()
                        .collect(Collectors.groupingBy(variant -> variant.getProduct().getId()));

        Map<UUID, List<ProductImage>> imagesByProduct =
                imageRepository.findAllByProductIdIn(recommendedProductIds)
                        .stream()
                        .collect(Collectors.groupingBy(image -> image.getProduct().getId()));

        List<RecommendationItemResponse> items = rules.stream()
                .map(rule -> toRecommendationItem(
                        rule,
                        variantsByProduct.getOrDefault(rule.getConsequentProduct().getId(), List.of()),
                        imagesByProduct.getOrDefault(rule.getConsequentProduct().getId(), List.of())
                ))
                .toList();

        return new RecommendationResponse(productId, FREQUENTLY_BOUGHT_TOGETHER, items);
    }

    private RecommendationItemResponse toRecommendationItem(
            AssociationRule rule,
            List<ProductVariant> variants,
            List<ProductImage> images) {

        Product product = rule.getConsequentProduct();

        ProductListItemResponse productCard = toProductListItem(product, variants, images);

        return new RecommendationItemResponse(
                productCard,
                rule.getSupport(),
                rule.getConfidence(),
                rule.getLift(),
                rule.getPairCount(),
                "Customers often buy this product together"
        );
    }

    private ProductListItemResponse toProductListItem(
            Product product,
            List<ProductVariant> variants,
            List<ProductImage> images) {

        List<BigDecimal> visiblePrices = variants.stream()
                .filter(variant -> variant.getStatus() != VariantStatus.INACTIVE)
                .map(ProductVariant::getPrice)
                .toList();

        BigDecimal minPrice = visiblePrices.stream()
                .min(Comparator.naturalOrder())
                .orElse(null);

        BigDecimal maxPrice = visiblePrices.stream()
                .max(Comparator.naturalOrder())
                .orElse(null);

        return new ProductListItemResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getShortDescription(),
                toRef(product.getBrand()),
                toRef(product.getCategory()),
                ThumbnailResolver.resolve(images),
                minPrice,
                maxPrice,
                product.getStatus(),
                product.getProductType()
        );
    }

    private CatalogRefResponse toRef(Brand brand) {
        return brand == null ? null : new CatalogRefResponse(brand.getId(), brand.getName());
    }

    private CatalogRefResponse toRef(Category category) {
        return category == null ? null : new CatalogRefResponse(category.getId(), category.getName());
    }

    private int resolveLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }

        if (limit < 1) {
            throw new BusinessRuleException(
                    HttpStatus.BAD_REQUEST,
                    "limit must be greater than or equal to 1"
            );
        }

        return Math.min(limit, MAX_LIMIT);
    }
}