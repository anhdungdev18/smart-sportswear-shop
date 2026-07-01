package com.dunghaiquyen.ecommerce.modules.coupon.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.common.exception.ResourceNotFoundException;
import com.dunghaiquyen.ecommerce.common.response.PageMeta;
import com.dunghaiquyen.ecommerce.modules.coupon.dto.CouponCreateRequest;
import com.dunghaiquyen.ecommerce.modules.coupon.dto.CouponListQuery;
import com.dunghaiquyen.ecommerce.modules.coupon.dto.CouponResponse;
import com.dunghaiquyen.ecommerce.modules.coupon.dto.CouponUpdateRequest;
import com.dunghaiquyen.ecommerce.modules.coupon.entity.Coupon;
import com.dunghaiquyen.ecommerce.modules.coupon.entity.CouponStatus;
import com.dunghaiquyen.ecommerce.modules.coupon.entity.CouponUsage;
import com.dunghaiquyen.ecommerce.modules.coupon.repository.CouponPromotionProductRepository;
import com.dunghaiquyen.ecommerce.modules.coupon.repository.CouponRepository;
import com.dunghaiquyen.ecommerce.modules.coupon.repository.CouponUsageRepository;
import com.dunghaiquyen.ecommerce.modules.order.entity.Order;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderItem;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.Promotion;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionScope;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionStatus;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionType;
import com.dunghaiquyen.ecommerce.modules.promotion.repository.PromotionRepository;
import com.dunghaiquyen.ecommerce.modules.user.entity.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
 * Houses both admin CRUD for coupons and the checkout-time apply/record flow
 * used by OrderService - same single-service-per-entity shape as
 * WishlistService/CartService, just with two distinct call sites (admin vs
 * checkout) for the one entity.
 *
 * <p>Phase N2 scope decisions:
 * <ul>
 *   <li>Only ORDER and PRODUCT promotion scopes are supported at checkout.
 *       CATEGORY is recognized by the schema but deferred - see
 *       {@link #computeDiscountBase}.</li>
 *   <li>Exactly one coupon per order. V3's uq_coupon_usages_order_id (unique
 *       on order_id alone) already enforces this at the DB level, so no new
 *       migration was needed to add the rule.</li>
 *   <li>Coupon codes are always normalized to upper-case/trimmed on both
 *       create and lookup, so uq_coupons_code stays a simple case-sensitive
 *       unique index with no upper()/lower() needed in queries.</li>
 * </ul>
 */
@Service
public class CouponService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final CouponPromotionProductRepository promotionProductRepository;
    private final PromotionRepository promotionRepository;

    public CouponService(
            CouponRepository couponRepository,
            CouponUsageRepository couponUsageRepository,
            CouponPromotionProductRepository promotionProductRepository,
            PromotionRepository promotionRepository) {
        this.couponRepository = couponRepository;
        this.couponUsageRepository = couponUsageRepository;
        this.promotionProductRepository = promotionProductRepository;
        this.promotionRepository = promotionRepository;
    }

    public record ListResult(List<CouponResponse> items, PageMeta meta) {
    }

    /**
     * Result of a successful checkout-time validation: discount to apply, and
     * the locked coupon/promotion rows to credit afterward in the same
     * transaction.
     */
    public record AppliedCoupon(Coupon coupon, Promotion promotion, BigDecimal discountAmount) {
    }

    @Transactional(readOnly = true)
    public ListResult list(CouponListQuery query) {
        Pageable pageable = PageRequest.of(resolvePageIndex(query.page()), resolveLimit(query.limit()));
        Page<Coupon> page = query.status() != null
                ? couponRepository.findAllByStatus(query.status(), pageable)
                : couponRepository.findAll(pageable);
        List<CouponResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return new ListResult(items, PageMeta.from(page));
    }

    @Transactional
    public CouponResponse create(CouponCreateRequest request) {
        String code = normalizeCode(request.code());
        if (couponRepository.existsByCode(code)) {
            throw new BusinessRuleException("Coupon code already exists: " + code);
        }
        Promotion promotion = promotionRepository.findById(request.promotionId())
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
        if (request.startsAt() != null && request.endsAt() != null && request.startsAt().isAfter(request.endsAt())) {
            throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "startsAt must not be after endsAt");
        }

        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setPromotion(promotion);
        coupon.setDescription(request.description());
        coupon.setStatus(request.status() != null ? request.status() : CouponStatus.ACTIVE);
        coupon.setStartsAt(request.startsAt());
        coupon.setEndsAt(request.endsAt());
        coupon.setUsageLimit(request.usageLimit());
        coupon.setPerUserLimit(request.perUserLimit());

        try {
            coupon = couponRepository.save(coupon);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessRuleException("Coupon code already exists: " + code);
        }
        return toResponse(coupon);
    }

    @Transactional
    public CouponResponse update(UUID id, CouponUpdateRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        Instant newStartsAt = request.startsAt() != null ? request.startsAt() : coupon.getStartsAt();
        Instant newEndsAt = request.endsAt() != null ? request.endsAt() : coupon.getEndsAt();
        if ((request.startsAt() != null || request.endsAt() != null)
                && newStartsAt != null && newEndsAt != null && newStartsAt.isAfter(newEndsAt)) {
            throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "startsAt must not be after endsAt");
        }

        if (request.description() != null) {
            coupon.setDescription(request.description());
        }
        if (request.status() != null) {
            coupon.setStatus(request.status());
        }
        if (request.startsAt() != null) {
            coupon.setStartsAt(request.startsAt());
        }
        if (request.endsAt() != null) {
            coupon.setEndsAt(request.endsAt());
        }
        if (request.usageLimit() != null) {
            coupon.setUsageLimit(request.usageLimit());
        }
        if (request.perUserLimit() != null) {
            coupon.setPerUserLimit(request.perUserLimit());
        }

        return toResponse(couponRepository.save(coupon));
    }

    /**
     * Called from OrderService.createOrderFromCart, inside its existing
     * transaction, after the cart's lines have been validated/priced but
     * before the order is persisted. Locks the coupon row for the rest of
     * that transaction (released on commit/rollback), which is what keeps
     * usage_count/usage_limit and per-user-limit race-free: a second
     * concurrent checkout with the same code blocks here until the first one
     * commits, then re-reads the now-updated usage_count.
     *
     * <p>noRollbackFor=BusinessRuleException: also called from
     * CheckoutPreviewService, which catches an invalid-coupon
     * BusinessRuleException and keeps going in the SAME transaction (to still
     * return cart totals). Without this, Spring's transactional advice on
     * THIS method marks the whole shared transaction rollback-only the
     * moment it throws - before the exception ever reaches that catch block -
     * so the caller's later, perfectly normal commit fails with
     * UnexpectedRollbackException despite having "handled" the error (caught
     * by actually exercising the preview-with-invalid-coupon path, not by
     * inspection). Harmless for createOrderFromCart's real use: that method
     * never catches the exception either, so it still propagates out of
     * createOrderFromCart's own @Transactional boundary and rolls back the
     * order exactly as before - this only changes which boundary's rollback
     * rule is the one that actually fires, not the end result.
     */
    @Transactional(noRollbackFor = BusinessRuleException.class)
    public AppliedCoupon validate(User user, String rawCode, BigDecimal subtotal, List<OrderItem> items) {
        String code = normalizeCode(rawCode);
        Coupon coupon = couponRepository.findByCodeForUpdate(code)
                .orElseThrow(() -> new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid coupon code"));

        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Coupon is not active");
        }
        Instant now = Instant.now();
        if (coupon.getStartsAt() != null && now.isBefore(coupon.getStartsAt())) {
            throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Coupon is not active yet");
        }
        if (coupon.getEndsAt() != null && now.isAfter(coupon.getEndsAt())) {
            throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Coupon has expired");
        }
        if (coupon.getUsageLimit() != null && coupon.getUsageCount() >= coupon.getUsageLimit()) {
            throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Coupon usage limit reached");
        }
        if (coupon.getPerUserLimit() != null) {
            long usedByUser = couponUsageRepository.countByCouponIdAndUserId(coupon.getId(), user.getId());
            if (usedByUser >= coupon.getPerUserLimit()) {
                throw new BusinessRuleException(
                        HttpStatus.UNPROCESSABLE_ENTITY, "You have already used this coupon the maximum number of times");
            }
        }

        if (coupon.getPromotion() == null) {
            throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Coupon has no associated promotion");
        }
        Promotion promotion = promotionRepository.findByIdForUpdate(coupon.getPromotion().getId())
                .orElseThrow(() -> new BusinessRuleException(
                        HttpStatus.UNPROCESSABLE_ENTITY, "Coupon has no associated promotion"));
        if (promotion.getStatus() != PromotionStatus.ACTIVE) {
            throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Promotion is not active");
        }
        if (promotion.getStartsAt() != null && now.isBefore(promotion.getStartsAt())) {
            throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Promotion is not active yet");
        }
        if (promotion.getEndsAt() != null && now.isAfter(promotion.getEndsAt())) {
            throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Promotion has expired");
        }
        if (promotion.getUsageLimit() != null && promotion.getUsageCount() >= promotion.getUsageLimit()) {
            throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Promotion usage limit reached");
        }
        if (promotion.getMinOrderAmount() != null && subtotal.compareTo(promotion.getMinOrderAmount()) < 0) {
            throw new BusinessRuleException(
                    HttpStatus.UNPROCESSABLE_ENTITY, "Order does not meet the minimum amount required for this coupon");
        }

        BigDecimal base = computeDiscountBase(promotion, subtotal, items);
        BigDecimal discount = computeDiscountAmount(promotion, base);
        // Never let the discount make the order total negative, regardless of scope/type.
        discount = discount.min(subtotal);
        return new AppliedCoupon(coupon, promotion, discount);
    }

    /** Must run in the SAME transaction as validate()/the order save - see validate()'s javadoc on locking. */
    @Transactional
    public void recordUsage(AppliedCoupon applied, Order order, User user) {
        CouponUsage usage = new CouponUsage();
        usage.setCoupon(applied.coupon());
        usage.setOrder(order);
        usage.setUser(user);
        usage.setDiscountAmount(applied.discountAmount());
        usage.setUsedAt(Instant.now());
        couponUsageRepository.save(usage);

        Coupon coupon = applied.coupon();
        coupon.setUsageCount(coupon.getUsageCount() + 1);
        couponRepository.save(coupon);

        Promotion promotion = applied.promotion();
        promotion.setUsageCount(promotion.getUsageCount() + 1);
        promotionRepository.save(promotion);
    }

    private BigDecimal computeDiscountBase(Promotion promotion, BigDecimal subtotal, List<OrderItem> items) {
        if (promotion.getScope() == PromotionScope.ORDER) {
            return subtotal;
        }
        if (promotion.getScope() == PromotionScope.PRODUCT) {
            Set<UUID> eligibleProductIds =
                    Set.copyOf(promotionProductRepository.findProductIdsByPromotionId(promotion.getId()));
            BigDecimal base = items.stream()
                    .filter(item -> eligibleProductIds.contains(item.getProduct().getId()))
                    .map(OrderItem::getLineTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (base.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessRuleException(
                        HttpStatus.UNPROCESSABLE_ENTITY, "Coupon does not apply to any items in your order");
            }
            return base;
        }
        // CATEGORY scope deferred this phase - see PromotionScope's javadoc.
        throw new BusinessRuleException(
                HttpStatus.UNPROCESSABLE_ENTITY, "This coupon's promotion scope is not supported yet");
    }

    private BigDecimal computeDiscountAmount(Promotion promotion, BigDecimal base) {
        BigDecimal raw;
        if (promotion.getType() == PromotionType.PERCENTAGE) {
            raw = base.multiply(promotion.getDiscountPercent())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            raw = promotion.getDiscountAmount();
        }
        if (promotion.getMaxDiscountAmount() != null && raw.compareTo(promotion.getMaxDiscountAmount()) > 0) {
            raw = promotion.getMaxDiscountAmount();
        }
        return raw.min(base);
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
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

    private CouponResponse toResponse(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getPromotion() != null ? coupon.getPromotion().getId() : null,
                coupon.getCode(),
                coupon.getDescription(),
                coupon.getStatus(),
                coupon.getStartsAt(),
                coupon.getEndsAt(),
                coupon.getUsageLimit(),
                coupon.getUsageCount(),
                coupon.getPerUserLimit(),
                coupon.getCreatedAt(),
                coupon.getUpdatedAt());
    }
}
