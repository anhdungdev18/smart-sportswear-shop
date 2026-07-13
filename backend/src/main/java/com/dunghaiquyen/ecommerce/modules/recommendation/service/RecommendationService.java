package com.dunghaiquyen.ecommerce.modules.recommendation.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.modules.brand.entity.Brand;
import com.dunghaiquyen.ecommerce.modules.cart.entity.Cart;
import com.dunghaiquyen.ecommerce.modules.cart.entity.CartItem;
import com.dunghaiquyen.ecommerce.modules.cart.repository.CartItemRepository;
import com.dunghaiquyen.ecommerce.modules.cart.repository.CartRepository;
import com.dunghaiquyen.ecommerce.modules.cart.web.CartOwner;
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
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.CartRecommendationResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RecommendationItemResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RecommendationResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.AssociationRule;
import com.dunghaiquyen.ecommerce.modules.recommendation.repository.AssociationRuleRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private static final String CART_RECOMMENDATION = "CART_RECOMMENDATION";

    private final AssociationRuleRepository associationRuleRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public RecommendationService(
            AssociationRuleRepository associationRuleRepository,
            ProductVariantRepository variantRepository,
            ProductImageRepository imageRepository,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository) {
        this.associationRuleRepository = associationRuleRepository;
        this.variantRepository = variantRepository;
        this.imageRepository = imageRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional(readOnly = true)
    public RecommendationResponse getFrequentlyBoughtTogether(UUID productId, Integer limit) {
        int resolvedLimit = resolveLimit(limit);

        List<AssociationRule> rules = associationRuleRepository.findActiveRulesByAntecedentProductId(
                productId,
                PageRequest.of(0, resolvedLimit)
        );

        List<RecommendationItemResponse> items = toRecommendationItems(
                rules,
                "Customers often buy this product together"
        );

        return new RecommendationResponse(productId, FREQUENTLY_BOUGHT_TOGETHER, items);
    }

    @Transactional(readOnly = true)
    public CartRecommendationResponse getCartRecommendations(CartOwner owner, Integer limit) {
        int resolvedLimit = resolveLimit(limit);

        Optional<Cart> optionalCart = findCart(owner);

        if (optionalCart.isEmpty()) {
            return new CartRecommendationResponse(
                    null,
                    List.of(),
                    CART_RECOMMENDATION,
                    List.of()
            );
        }

        Cart cart = optionalCart.get();

        List<CartItem> cartItems = cartItemRepository.findAllByCartIdWithVariantAndProduct(cart.getId());

        List<UUID> sourceProductIds = cartItems.stream()
                .map(item -> item.getVariant().getProduct().getId())
                .distinct()
                .toList();

        if (sourceProductIds.isEmpty()) {
            return new CartRecommendationResponse(
                    cart.getId(),
                    List.of(),
                    CART_RECOMMENDATION,
                    List.of()
            );
        }

        Set<UUID> productIdsInCart = new HashSet<>(sourceProductIds);

        List<AssociationRule> rules =
                associationRuleRepository.findActiveRulesByAntecedentProductIdIn(sourceProductIds);

        List<AssociationRule> selectedRules = selectBestRulesForCart(
                rules,
                productIdsInCart,
                resolvedLimit
        );

        List<RecommendationItemResponse> items = toRecommendationItems(
                selectedRules,
                "Recommended based on products in your cart"
        );

        return new CartRecommendationResponse(
                cart.getId(),
                sourceProductIds,
                CART_RECOMMENDATION,
                items
        );
    }

    private Optional<Cart> findCart(CartOwner owner) {
        if (owner.isUser()) {
            return cartRepository.findByUserId(owner.userId());
        }
        return cartRepository.findBySessionId(owner.sessionId());
    }

    private List<AssociationRule> selectBestRulesForCart(
            List<AssociationRule> rules,
            Set<UUID> productIdsInCart,
            int limit) {

        Comparator<AssociationRule> rankComparator = associationRuleRankComparator();

        Map<UUID, AssociationRule> bestRuleByConsequentProductId = new LinkedHashMap<>();

        for (AssociationRule rule : rules) {
            UUID recommendedProductId = rule.getConsequentProduct().getId();

            if (productIdsInCart.contains(recommendedProductId)) {
                continue;
            }

            AssociationRule existingRule = bestRuleByConsequentProductId.get(recommendedProductId);

            if (existingRule == null || rankComparator.compare(rule, existingRule) > 0) {
                bestRuleByConsequentProductId.put(recommendedProductId, rule);
            }
        }

        return bestRuleByConsequentProductId.values()
                .stream()
                .sorted(rankComparator.reversed())
                .limit(limit)
                .toList();
    }

    private Comparator<AssociationRule> associationRuleRankComparator() {
        return Comparator
                .comparingDouble(AssociationRule::getConfidence)
                .thenComparingDouble(AssociationRule::getLift)
                .thenComparingDouble(AssociationRule::getSupport)
                .thenComparingLong(AssociationRule::getPairCount);
    }

    private List<RecommendationItemResponse> toRecommendationItems(
            List<AssociationRule> rules,
            String reason) {

        if (rules.isEmpty()) {
            return List.of();
        }

        List<UUID> recommendedProductIds = rules.stream()
                .map(rule -> rule.getConsequentProduct().getId())
                .distinct()
                .toList();

        Map<UUID, List<ProductVariant>> variantsByProduct =
                variantRepository.findAllByProductIdIn(recommendedProductIds)
                        .stream()
                        .collect(Collectors.groupingBy(variant -> variant.getProduct().getId()));

        Map<UUID, List<ProductImage>> imagesByProduct =
                imageRepository.findAllByProductIdIn(recommendedProductIds)
                        .stream()
                        .collect(Collectors.groupingBy(image -> image.getProduct().getId()));

        return rules.stream()
                .map(rule -> toRecommendationItem(
                        rule,
                        variantsByProduct.getOrDefault(rule.getConsequentProduct().getId(), List.of()),
                        imagesByProduct.getOrDefault(rule.getConsequentProduct().getId(), List.of()),
                        reason
                ))
                .toList();
    }

    private RecommendationItemResponse toRecommendationItem(
            AssociationRule rule,
            List<ProductVariant> variants,
            List<ProductImage> images,
            String reason) {

        Product product = rule.getConsequentProduct();

        ProductListItemResponse productCard = toProductListItem(product, variants, images);

        return new RecommendationItemResponse(
                productCard,
                rule.getSupport(),
                rule.getConfidence(),
                rule.getLift(),
                rule.getPairCount(),
                reason
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