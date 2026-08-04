package com.dunghaiquyen.ecommerce.modules.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.dunghaiquyen.ecommerce.modules.brand.entity.Brand;
import com.dunghaiquyen.ecommerce.modules.cart.repository.CartItemRepository;
import com.dunghaiquyen.ecommerce.modules.cart.repository.CartRepository;
import com.dunghaiquyen.ecommerce.modules.category.entity.Category;
import com.dunghaiquyen.ecommerce.modules.product.dto.ProductListItemResponse;
import com.dunghaiquyen.ecommerce.modules.product.entity.Product;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductStatus;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductVariant;
import com.dunghaiquyen.ecommerce.modules.product.entity.VariantStatus;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductImageRepository;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductRepository;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductVariantRepository;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RecommendationItemResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RecommendationResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.AssociationRule;
import com.dunghaiquyen.ecommerce.modules.recommendation.repository.AssociationRuleRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private AssociationRuleRepository ruleRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductVariantRepository variantRepository;
    @Mock
    private ProductImageRepository imageRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private RecommendationCacheService cacheService;

    private RecommendationService service;
    private Product sourceProduct;

    @BeforeEach
    void setUp() {
        service = new RecommendationService(
                ruleRepository,
                productRepository,
                variantRepository,
                imageRepository,
                cartRepository,
                cartItemRepository,
                cacheService
        );
        sourceProduct = product("Source product");
        when(productRepository.findActiveByIdWithBrandAndCategory(sourceProduct.getId()))
                .thenReturn(Optional.of(sourceProduct));
        when(cacheService.productFrequentlyBoughtTogetherKey(any(), anyInt())).thenReturn("cache-key");
    }

    @Test
    void frequentlyBoughtTogether_excludesProductWithoutAvailableStock() {
        Product recommended = product("Out of stock product");
        AssociationRule rule = rule(sourceProduct, recommended);
        ProductVariant unavailable = variant(recommended, 2, 2, new BigDecimal("500000"));

        when(cacheService.get(anyString(), eq(RecommendationResponse.class))).thenReturn(Optional.empty());
        when(ruleRepository.findActiveRulesByAntecedentProductId(eq(sourceProduct.getId()), any()))
                .thenReturn(List.of(rule));
        when(variantRepository.findAllByProductIdIn(anyList())).thenReturn(List.of(unavailable));
        when(imageRepository.findAllByProductIdIn(anyList())).thenReturn(List.of());
        when(productRepository.findSimilarActiveProducts(any(), any(), any(), any())).thenReturn(List.of());
        when(productRepository.findActiveFallbackProductsExcluding(anySet(), any())).thenReturn(List.of());

        RecommendationResponse response = service.getFrequentlyBoughtTogether(sourceProduct.getId(), 8);

        assertThat(response.items()).isEmpty();
    }

    @Test
    void cachedRecommendation_refreshesCurrentPriceAndAvailabilityBeforeReturning() {
        Product recommended = product("Current product");
        ProductListItemResponse staleCard = new ProductListItemResponse(
                recommended.getId(),
                recommended.getName(),
                recommended.getSlug(),
                null,
                null,
                null,
                null,
                new BigDecimal("100000"),
                new BigDecimal("100000"),
                null,
                null,
                null,
                1,
                ProductStatus.ACTIVE,
                null
        );
        RecommendationResponse cached = new RecommendationResponse(
                sourceProduct.getId(),
                "FREQUENTLY_BOUGHT_TOGETHER",
                List.of(new RecommendationItemResponse(staleCard, 0.5, 0.8, 1.2, 4, "cached"))
        );

        when(cacheService.get(anyString(), eq(RecommendationResponse.class)))
                .thenReturn(Optional.of(cached));
        when(productRepository.findActiveByIdInWithBrandAndCategory(List.of(recommended.getId())))
                .thenReturn(List.of(recommended));
        when(variantRepository.findAllByProductIdIn(List.of(recommended.getId())))
                .thenReturn(List.of(variant(recommended, 5, 0, new BigDecimal("250000"))));
        when(imageRepository.findAllByProductIdIn(List.of(recommended.getId())))
                .thenReturn(List.of());

        RecommendationResponse response = service.getFrequentlyBoughtTogether(sourceProduct.getId(), 8);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().product().minPrice())
                .isEqualByComparingTo("250000");
    }

    private Product product(String name) {
        Brand brand = new Brand();
        brand.setId(UUID.randomUUID());
        brand.setName("Brand");
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Category");

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName(name);
        product.setSlug(name.toLowerCase().replace(' ', '-'));
        product.setBrand(brand);
        product.setCategory(category);
        product.setStatus(ProductStatus.ACTIVE);
        return product;
    }

    private ProductVariant variant(Product product, int stock, int reserved, BigDecimal price) {
        ProductVariant variant = new ProductVariant();
        variant.setId(UUID.randomUUID());
        variant.setProduct(product);
        variant.setStatus(VariantStatus.ACTIVE);
        variant.setStockQuantity(stock);
        variant.setReservedQuantity(reserved);
        variant.setPrice(price);
        return variant;
    }

    private AssociationRule rule(Product source, Product recommended) {
        AssociationRule rule = new AssociationRule();
        rule.setAntecedentProduct(source);
        rule.setConsequentProduct(recommended);
        rule.setSupport(0.5);
        rule.setConfidence(0.8);
        rule.setLift(1.2);
        rule.setPairCount(4);
        return rule;
    }
}
