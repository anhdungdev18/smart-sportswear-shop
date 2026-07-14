package com.dunghaiquyen.ecommerce.modules.recommendation.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.common.exception.ResourceNotFoundException;
import com.dunghaiquyen.ecommerce.modules.cart.entity.Cart;
import com.dunghaiquyen.ecommerce.modules.cart.entity.CartItem;
import com.dunghaiquyen.ecommerce.modules.cart.repository.CartItemRepository;
import com.dunghaiquyen.ecommerce.modules.cart.repository.CartRepository;
import com.dunghaiquyen.ecommerce.modules.cart.web.CartOwner;
import com.dunghaiquyen.ecommerce.modules.product.entity.Product;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductStatus;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductRepository;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RecommendationLogProductStatsResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RecommendationLogRequest;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RecommendationLogResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RecommendationLogSummaryResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RecommendationAlgorithm;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.AssociationRule;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RecommendationEventType;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RecommendationLog;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RecommendationSourceType;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RecommendationType;
import com.dunghaiquyen.ecommerce.modules.recommendation.repository.RecommendationLogProductStatsProjection;
import com.dunghaiquyen.ecommerce.modules.recommendation.repository.RecommendationLogRepository;
import com.dunghaiquyen.ecommerce.modules.recommendation.repository.AssociationRuleRepository;
import com.dunghaiquyen.ecommerce.modules.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationLogService {

    private static final int DEFAULT_DAYS = 7;
    private static final int MAX_DAYS = 90;
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;

    private final RecommendationLogRepository recommendationLogRepository;
    private final ProductRepository productRepository;
    private final AssociationRuleRepository associationRuleRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final EntityManager entityManager;

    public RecommendationLogService(
            RecommendationLogRepository recommendationLogRepository,
            ProductRepository productRepository,
            AssociationRuleRepository associationRuleRepository,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            EntityManager entityManager) {
        this.recommendationLogRepository = recommendationLogRepository;
        this.productRepository = productRepository;
        this.associationRuleRepository = associationRuleRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public RecommendationLogResponse logEvent(CartOwner owner, RecommendationLogRequest request) {
        validateLogRequest(request);

        Product recommendedProduct = productRepository.findById(request.recommendedProductId())
                .filter(product -> product.getStatus() == ProductStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Recommended product not found"));
        VerifiedSource source = verifySource(owner, request);

        if (source.productIds().contains(recommendedProduct.getId())) {
            throw new BusinessRuleException(
                    HttpStatus.BAD_REQUEST,
                    "Recommended product must not already be in the recommendation source"
            );
        }

        Optional<AssociationRule> matchedRule = associationRuleRepository.findBestActiveRule(
                source.productIds(),
                recommendedProduct.getId(),
                PageRequest.of(0, 1)
        ).stream().findFirst();

        RecommendationLog log = new RecommendationLog();

        if (owner.isUser()) {
            log.setUser(entityManager.getReference(User.class, owner.userId()));
        } else {
            log.setSessionId(owner.sessionId());
        }

        log.setEventType(request.eventType());
        log.setSourceType(request.sourceType());
        log.setRecommendationType(source.recommendationType());
        log.setRecommendedProduct(recommendedProduct);
        log.setPositionIndex(request.position());
        applyServerDerivedMetadata(log, matchedRule);
        log.setSourceProduct(source.product());
        log.setSourceProductIds(source.productIds());
        log.setCart(source.cart());

        RecommendationLog savedLog = recommendationLogRepository.saveAndFlush(log);

        return new RecommendationLogResponse(
                savedLog.getId(),
                savedLog.getEventType(),
                savedLog.getSourceType(),
                savedLog.getRecommendationType(),
                savedLog.getRecommendedProduct().getId(),
                savedLog.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public RecommendationLogSummaryResponse getSummary(Integer days, Integer limit) {
        int resolvedDays = resolveDays(days);
        int resolvedLimit = resolveLimit(limit);

        Instant to = Instant.now();
        Instant from = to.minus(resolvedDays, ChronoUnit.DAYS);

        long totalLogs = recommendationLogRepository.countByCreatedAtGreaterThanEqual(from);
        long impressions = recommendationLogRepository.countByEventTypeAndCreatedAtGreaterThanEqual(
                RecommendationEventType.IMPRESSION,
                from
        );
        long clicks = recommendationLogRepository.countByEventTypeAndCreatedAtGreaterThanEqual(
                RecommendationEventType.CLICK,
                from
        );
        long addToCarts = recommendationLogRepository.countByEventTypeAndCreatedAtGreaterThanEqual(
                RecommendationEventType.ADD_TO_CART,
                from
        );

        List<RecommendationLogProductStatsResponse> topProducts =
                recommendationLogRepository.findTopRecommendedProducts(from, resolvedLimit)
                        .stream()
                        .map(this::toProductStatsResponse)
                        .toList();

        return new RecommendationLogSummaryResponse(
                from,
                to,
                totalLogs,
                impressions,
                clicks,
                addToCarts,
                topProducts
        );
    }

    private RecommendationLogProductStatsResponse toProductStatsResponse(
            RecommendationLogProductStatsProjection row) {

        long impressions = safeLong(row.getImpressions());
        long clicks = safeLong(row.getClicks());
        long addToCarts = safeLong(row.getAddToCarts());

        double ctr = impressions == 0
                ? 0.0
                : (clicks * 100.0) / impressions;

        return new RecommendationLogProductStatsResponse(
                row.getProductId(),
                row.getProductName(),
                impressions,
                clicks,
                addToCarts,
                ctr
        );
    }

    private void validateLogRequest(RecommendationLogRequest request) {
        if (request.sourceType() == RecommendationSourceType.PRODUCT_DETAIL
                && request.sourceProductId() == null) {
            throw new BusinessRuleException(
                    HttpStatus.BAD_REQUEST,
                    "sourceProductId is required for PRODUCT_DETAIL recommendation logs"
            );
        }

        if (request.sourceType() == RecommendationSourceType.CART
                && request.cartId() == null) {
            throw new BusinessRuleException(
                    HttpStatus.BAD_REQUEST,
                    "cartId is required for CART recommendation logs"
            );
        }
    }

    private VerifiedSource verifySource(CartOwner owner, RecommendationLogRequest request) {
        if (request.sourceType() == RecommendationSourceType.PRODUCT_DETAIL) {
            Product sourceProduct = productRepository.findById(request.sourceProductId())
                    .filter(product -> product.getStatus() == ProductStatus.ACTIVE)
                    .orElseThrow(() -> new ResourceNotFoundException("Source product not found"));
            return new VerifiedSource(
                    null,
                    sourceProduct,
                    List.of(sourceProduct.getId()),
                    RecommendationType.FREQUENTLY_BOUGHT_TOGETHER
            );
        }

        Optional<Cart> ownedCart = owner.isUser()
                ? cartRepository.findByUserId(owner.userId())
                : cartRepository.findBySessionId(owner.sessionId());

        Cart cart = ownedCart
                .filter(candidate -> candidate.getId().equals(request.cartId()))
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        List<UUID> productIds = cartItemRepository.findAllByCartIdWithVariantAndProduct(cart.getId())
                .stream()
                .map(CartItem::getVariant)
                .map(variant -> variant.getProduct().getId())
                .distinct()
                .toList();

        if (productIds.isEmpty()) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        return new VerifiedSource(
                cart,
                null,
                productIds,
                RecommendationType.CART_RECOMMENDATION
        );
    }

    private void applyServerDerivedMetadata(
            RecommendationLog log,
            Optional<AssociationRule> matchedRule) {
        if (matchedRule.isEmpty()) {
            log.setAlgorithm(RecommendationAlgorithm.FALLBACK);
            log.setReason("Fallback recommendation");
            return;
        }

        AssociationRule rule = matchedRule.get();
        log.setAlgorithm(RecommendationAlgorithm.ASSOCIATION_RULE);
        log.setSupport(rule.getSupport());
        log.setConfidence(rule.getConfidence());
        log.setLift(rule.getLift());
        log.setPairCount(rule.getPairCount());
        log.setReason("Association rule recommendation");
    }

    private int resolveDays(Integer days) {
        if (days == null) {
            return DEFAULT_DAYS;
        }

        if (days < 1) {
            throw new BusinessRuleException(
                    HttpStatus.BAD_REQUEST,
                    "days must be greater than or equal to 1"
            );
        }

        return Math.min(days, MAX_DAYS);
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

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private record VerifiedSource(
            Cart cart,
            Product product,
            List<UUID> productIds,
            RecommendationType recommendationType
    ) {
    }
}
