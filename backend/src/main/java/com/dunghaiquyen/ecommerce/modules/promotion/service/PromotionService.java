package com.dunghaiquyen.ecommerce.modules.promotion.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.common.exception.ResourceNotFoundException;
import com.dunghaiquyen.ecommerce.common.response.PageMeta;
import com.dunghaiquyen.ecommerce.modules.product.entity.Product;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductRepository;
import com.dunghaiquyen.ecommerce.modules.promotion.dto.PromotionCreateRequest;
import com.dunghaiquyen.ecommerce.modules.promotion.dto.PromotionListQuery;
import com.dunghaiquyen.ecommerce.modules.promotion.dto.PromotionResponse;
import com.dunghaiquyen.ecommerce.modules.promotion.dto.PromotionUpdateRequest;
import com.dunghaiquyen.ecommerce.modules.promotion.dto.PublicPromotionResponse;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.Promotion;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionProduct;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionScope;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionStatus;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionType;
import com.dunghaiquyen.ecommerce.modules.promotion.repository.PromotionProductRepository;
import com.dunghaiquyen.ecommerce.modules.promotion.repository.PromotionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin CRUD for promotions, including their PRODUCT-scope product links
 * (promotion_products). promotion_rules (MIN_QUANTITY/TARGET_BRAND/
 * TARGET_CATEGORY/TARGET_VARIANT) is intentionally not mapped this phase -
 * Phase N2 only needs ORDER and PRODUCT scope, and promotion_products alone
 * already covers PRODUCT scope. See CouponService for how scope/type are
 * actually applied at checkout.
 */
@Service
public class PromotionService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final PromotionRepository promotionRepository;
    private final PromotionProductRepository promotionProductRepository;
    private final ProductRepository productRepository;

    public PromotionService(
            PromotionRepository promotionRepository,
            PromotionProductRepository promotionProductRepository,
            ProductRepository productRepository) {
        this.promotionRepository = promotionRepository;
        this.promotionProductRepository = promotionProductRepository;
        this.productRepository = productRepository;
    }

    public record ListResult(List<PromotionResponse> items, PageMeta meta) {
    }

    /** GET /api/v1/promotions/active - see PromotionRepository.findAllActive's javadoc for what "active" means here. */
    @Transactional(readOnly = true)
    public List<PublicPromotionResponse> listActive() {
        return promotionRepository.findAllActive(PromotionStatus.ACTIVE, Instant.now()).stream()
                .map(this::toPublicResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ListResult list(PromotionListQuery query) {
        Pageable pageable = PageRequest.of(resolvePageIndex(query.page()), resolveLimit(query.limit()));
        Page<Promotion> page = query.status() != null
                ? promotionRepository.findAllByStatus(query.status(), pageable)
                : promotionRepository.findAll(pageable);
        List<PromotionResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return new ListResult(items, PageMeta.from(page));
    }

    @Transactional
    public PromotionResponse create(PromotionCreateRequest request) {
        if (promotionRepository.existsBySlug(request.slug())) {
            throw new BusinessRuleException("Slug already exists: " + request.slug());
        }
        validateDiscountShape(request.type(), request.discountPercent(), request.discountAmount());
        validateTimeRange(request.startsAt(), request.endsAt());
        validateProductScopeOnCreate(request.scope(), request.productIds());

        Promotion promotion = new Promotion();
        promotion.setName(request.name().trim());
        promotion.setSlug(request.slug());
        promotion.setDescription(request.description());
        promotion.setType(request.type());
        promotion.setScope(request.scope());
        promotion.setStatus(request.status() != null ? request.status() : PromotionStatus.DRAFT);
        promotion.setDiscountPercent(request.discountPercent());
        promotion.setDiscountAmount(request.discountAmount());
        promotion.setMinOrderAmount(request.minOrderAmount());
        promotion.setMaxDiscountAmount(request.maxDiscountAmount());
        promotion.setStartsAt(request.startsAt());
        promotion.setEndsAt(request.endsAt());
        promotion.setUsageLimit(request.usageLimit());

        try {
            promotion = promotionRepository.save(promotion);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessRuleException("Slug already exists: " + request.slug());
        }

        if (request.scope() == PromotionScope.PRODUCT) {
            attachProducts(promotion, request.productIds());
        }
        return toResponse(promotion);
    }

    @Transactional
    public PromotionResponse update(UUID id, PromotionUpdateRequest request) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));

        BigDecimal newPercent = request.discountPercent() != null ? request.discountPercent() : promotion.getDiscountPercent();
        BigDecimal newAmount = request.discountAmount() != null ? request.discountAmount() : promotion.getDiscountAmount();
        if (request.discountPercent() != null || request.discountAmount() != null) {
            validateDiscountShape(promotion.getType(), newPercent, newAmount);
        }
        Instant newStartsAt = request.startsAt() != null ? request.startsAt() : promotion.getStartsAt();
        Instant newEndsAt = request.endsAt() != null ? request.endsAt() : promotion.getEndsAt();
        if (request.startsAt() != null || request.endsAt() != null) {
            validateTimeRange(newStartsAt, newEndsAt);
        }
        if (request.productIds() != null) {
            validateProductScopeOnUpdate(promotion.getScope(), request.productIds());
        }

        if (request.name() != null) {
            promotion.setName(request.name().trim());
        }
        if (request.description() != null) {
            promotion.setDescription(request.description());
        }
        if (request.status() != null) {
            promotion.setStatus(request.status());
        }
        if (request.discountPercent() != null) {
            promotion.setDiscountPercent(request.discountPercent());
        }
        if (request.discountAmount() != null) {
            promotion.setDiscountAmount(request.discountAmount());
        }
        if (request.minOrderAmount() != null) {
            promotion.setMinOrderAmount(request.minOrderAmount());
        }
        if (request.maxDiscountAmount() != null) {
            promotion.setMaxDiscountAmount(request.maxDiscountAmount());
        }
        if (request.startsAt() != null) {
            promotion.setStartsAt(request.startsAt());
        }
        if (request.endsAt() != null) {
            promotion.setEndsAt(request.endsAt());
        }
        if (request.usageLimit() != null) {
            promotion.setUsageLimit(request.usageLimit());
        }

        promotion = promotionRepository.save(promotion);

        if (request.productIds() != null) {
            promotionProductRepository.deleteAllByPromotionId(promotion.getId());
            if (promotion.getScope() == PromotionScope.PRODUCT) {
                attachProducts(promotion, request.productIds());
            }
        }
        return toResponse(promotion);
    }

    private void validateDiscountShape(PromotionType type, BigDecimal discountPercent, BigDecimal discountAmount) {
        if (type == PromotionType.PERCENTAGE) {
            if (discountPercent == null || discountAmount != null) {
                throw new BusinessRuleException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "PERCENTAGE promotions require discountPercent and must not set discountAmount");
            }
            if (discountPercent.compareTo(BigDecimal.ZERO) <= 0 || discountPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BusinessRuleException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "discountPercent must be between 0 and 100");
            }
        } else {
            if (discountAmount == null || discountPercent != null) {
                throw new BusinessRuleException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "FIXED_AMOUNT promotions require discountAmount and must not set discountPercent");
            }
        }
    }

    private void validateTimeRange(java.time.Instant startsAt, java.time.Instant endsAt) {
        if (startsAt != null && endsAt != null && startsAt.isAfter(endsAt)) {
            throw new BusinessRuleException(
                    HttpStatus.UNPROCESSABLE_ENTITY, "startsAt must not be after endsAt");
        }
    }

    private void validateProductScopeOnCreate(PromotionScope scope, List<UUID> productIds) {
        if (scope == PromotionScope.PRODUCT) {
            if (productIds == null || productIds.isEmpty()) {
                throw new BusinessRuleException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "productIds is required for PRODUCT-scoped promotions");
            }
        } else if (productIds != null && !productIds.isEmpty()) {
            throw new BusinessRuleException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "productIds only applies to PRODUCT-scoped promotions");
        }
    }

    /**
     * PATCH semantics differ intentionally from create: a non-null empty list
     * means "replace the current set with empty", matching
     * PromotionUpdateRequest's documented contract.
     */
    private void validateProductScopeOnUpdate(PromotionScope scope, List<UUID> productIds) {
        if (scope != PromotionScope.PRODUCT && productIds != null && !productIds.isEmpty()) {
            throw new BusinessRuleException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "productIds only applies to PRODUCT-scoped promotions");
        }
    }

    private void attachProducts(Promotion promotion, List<UUID> productIds) {
        List<Product> products = productRepository.findAllById(productIds);
        if (products.size() != Set.copyOf(productIds).size()) {
            throw new ResourceNotFoundException("One or more products not found");
        }
        for (Product product : products) {
            PromotionProduct link = new PromotionProduct();
            link.setPromotion(promotion);
            link.setProduct(product);
            promotionProductRepository.save(link);
        }
    }

    private int resolvePageIndex(Integer page) {
        return (page != null && page > 0) ? page - 1 : 0;
    }

    private int resolveLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private PublicPromotionResponse toPublicResponse(Promotion promotion) {
        List<UUID> productIds = promotion.getScope() == PromotionScope.PRODUCT
                ? promotionProductRepository.findProductIdsByPromotionId(promotion.getId())
                : List.of();
        return new PublicPromotionResponse(
                promotion.getId(),
                promotion.getName(),
                promotion.getSlug(),
                promotion.getDescription(),
                promotion.getType(),
                promotion.getScope(),
                promotion.getDiscountPercent(),
                promotion.getDiscountAmount(),
                promotion.getMinOrderAmount(),
                promotion.getMaxDiscountAmount(),
                promotion.getStartsAt(),
                promotion.getEndsAt(),
                productIds);
    }

    private PromotionResponse toResponse(Promotion promotion) {
        List<UUID> productIds = promotion.getScope() == PromotionScope.PRODUCT
                ? promotionProductRepository.findProductIdsByPromotionId(promotion.getId())
                : List.of();
        return new PromotionResponse(
                promotion.getId(),
                promotion.getName(),
                promotion.getSlug(),
                promotion.getDescription(),
                promotion.getType(),
                promotion.getScope(),
                promotion.getStatus(),
                promotion.getDiscountPercent(),
                promotion.getDiscountAmount(),
                promotion.getMinOrderAmount(),
                promotion.getMaxDiscountAmount(),
                promotion.getStartsAt(),
                promotion.getEndsAt(),
                promotion.getUsageLimit(),
                promotion.getUsageCount(),
                productIds,
                promotion.getCreatedAt(),
                promotion.getUpdatedAt());
    }
}
