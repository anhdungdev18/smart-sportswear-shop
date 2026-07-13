package com.dunghaiquyen.ecommerce.modules.recommendation.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.common.exception.ResourceNotFoundException;
import com.dunghaiquyen.ecommerce.modules.cart.entity.Cart;
import com.dunghaiquyen.ecommerce.modules.cart.web.CartOwner;
import com.dunghaiquyen.ecommerce.modules.product.entity.Product;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductRepository;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RecommendationLogProductStatsResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RecommendationLogRequest;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RecommendationLogResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RecommendationLogSummaryResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RecommendationAlgorithm;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RecommendationEventType;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RecommendationLog;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RecommendationSourceType;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RecommendationType;
import com.dunghaiquyen.ecommerce.modules.recommendation.repository.RecommendationLogProductStatsProjection;
import com.dunghaiquyen.ecommerce.modules.recommendation.repository.RecommendationLogRepository;
import com.dunghaiquyen.ecommerce.modules.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.http.HttpStatus;
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
    private final EntityManager entityManager;

    public RecommendationLogService(
            RecommendationLogRepository recommendationLogRepository,
            ProductRepository productRepository,
            EntityManager entityManager) {
        this.recommendationLogRepository = recommendationLogRepository;
        this.productRepository = productRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public RecommendationLogResponse logEvent(CartOwner owner, RecommendationLogRequest request) {
        validateLogRequest(request);

        Product recommendedProduct = productRepository.findById(request.recommendedProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Recommended product not found"));

        RecommendationLog log = new RecommendationLog();

        if (owner.isUser()) {
            log.setUser(entityManager.getReference(User.class, owner.userId()));
        } else {
            log.setSessionId(owner.sessionId());
        }

        log.setEventType(request.eventType());
        log.setSourceType(request.sourceType());
        log.setRecommendationType(resolveRecommendationType(request));
        log.setRecommendedProduct(recommendedProduct);
        log.setPositionIndex(request.position());
        log.setAlgorithm(resolveAlgorithm(request));
        log.setSupport(request.support());
        log.setConfidence(request.confidence());
        log.setLift(request.lift());
        log.setPairCount(request.pairCount());
        log.setReason(request.reason());

        if (request.sourceProductId() != null) {
            log.setSourceProduct(entityManager.getReference(Product.class, request.sourceProductId()));
        }

        if (request.sourceProductIds() != null) {
            log.setSourceProductIds(request.sourceProductIds());
        }

        if (request.cartId() != null) {
            log.setCart(entityManager.getReference(Cart.class, request.cartId()));
        }

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
                && (request.sourceProductIds() == null || request.sourceProductIds().isEmpty())) {
            throw new BusinessRuleException(
                    HttpStatus.BAD_REQUEST,
                    "sourceProductIds is required for CART recommendation logs"
            );
        }

        validateScore("support", request.support());
        validateScore("confidence", request.confidence());
        validateScore("lift", request.lift());

        if (request.pairCount() != null && request.pairCount() < 0) {
            throw new BusinessRuleException(
                    HttpStatus.BAD_REQUEST,
                    "pairCount must be greater than or equal to 0"
            );
        }
    }

    private void validateScore(String fieldName, Double value) {
        if (value != null && value < 0) {
            throw new BusinessRuleException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " must be greater than or equal to 0"
            );
        }
    }

    private RecommendationType resolveRecommendationType(RecommendationLogRequest request) {
        if (request.recommendationType() != null) {
            return request.recommendationType();
        }

        if (request.sourceType() == RecommendationSourceType.CART) {
            return RecommendationType.CART_RECOMMENDATION;
        }

        return RecommendationType.FREQUENTLY_BOUGHT_TOGETHER;
    }

    private RecommendationAlgorithm resolveAlgorithm(RecommendationLogRequest request) {
        if (request.algorithm() != null) {
            return request.algorithm();
        }

        boolean looksLikeAssociationRule =
                request.pairCount() != null && request.pairCount() > 0;

        return looksLikeAssociationRule
                ? RecommendationAlgorithm.ASSOCIATION_RULE
                : RecommendationAlgorithm.FALLBACK;
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
}