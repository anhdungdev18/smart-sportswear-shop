package com.dunghaiquyen.ecommerce.modules.review.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.common.exception.ResourceNotFoundException;
import com.dunghaiquyen.ecommerce.common.response.PageMeta;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderItem;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import com.dunghaiquyen.ecommerce.modules.product.entity.Product;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductStatus;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductRepository;
import com.dunghaiquyen.ecommerce.modules.review.dto.CreateReviewRequest;
import com.dunghaiquyen.ecommerce.modules.review.dto.ReviewListQuery;
import com.dunghaiquyen.ecommerce.modules.review.dto.ReviewResponse;
import com.dunghaiquyen.ecommerce.modules.review.dto.UpdateReviewStatusRequest;
import com.dunghaiquyen.ecommerce.modules.review.entity.ProductReview;
import com.dunghaiquyen.ecommerce.modules.review.entity.ReviewStatus;
import com.dunghaiquyen.ecommerce.modules.review.repository.ProductReviewRepository;
import com.dunghaiquyen.ecommerce.modules.review.repository.ReviewOrderItemRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Uniqueness rule (self-chosen, per Phase N1 spec): one review per user PER
 * ORDER_ITEM, not per product. order_item_id already exists in the V3 schema
 * as a nullable FK whose sole purpose is to link a review to a specific
 * purchase - choosing "per product" instead would make that column
 * pointless. It also matches realistic marketplace UX (e.g. Shopee/Tiki):
 * buying the same product in two separate orders lets you review each
 * purchase independently. V4 enforces this at the DB level
 * (uq_product_reviews_user_order_item).
 *
 * <p>"Successfully purchased" = the order reached {@link OrderStatus#DELIVERED}.
 * The create endpoint does not take an explicit order_item_id - it
 * auto-discovers the first DELIVERED, not-yet-reviewed order item for the
 * (user, product) pair and attaches the review to it. If none exists (never
 * purchased, or every eligible purchase already reviewed), the request fails
 * with 422.
 */
@Service
public class ReviewService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final ProductReviewRepository reviewRepository;
    private final ReviewOrderItemRepository reviewOrderItemRepository;
    private final ProductRepository productRepository;

    public ReviewService(
            ProductReviewRepository reviewRepository,
            ReviewOrderItemRepository reviewOrderItemRepository,
            ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.reviewOrderItemRepository = reviewOrderItemRepository;
        this.productRepository = productRepository;
    }

    public record ListResult(List<ReviewResponse> items, PageMeta meta) {
    }

    @Transactional
    public ReviewResponse create(UUID userId, String productIdOrSlug, CreateReviewRequest request) {
        return create(userId, resolveVisibleProduct(productIdOrSlug).getId(), request);
    }

    @Transactional
    public ReviewResponse create(UUID userId, UUID productId, CreateReviewRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        List<OrderItem> eligible =
                reviewOrderItemRepository.findEligibleUnreviewed(userId, product.getId(), OrderStatus.DELIVERED);
        if (eligible.isEmpty()) {
            throw new BusinessRuleException(
                    HttpStatus.UNPROCESSABLE_ENTITY, "You can only review a product you have purchased");
        }
        OrderItem orderItem = eligible.get(0);

        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setUser(orderItem.getOrder().getUser());
        review.setOrderItem(orderItem);
        review.setRating(request.rating());
        review.setTitle(request.title());
        review.setContent(request.content());
        review.setStatus(ReviewStatus.PENDING);
        review.setVerifiedPurchase(true);

        try {
            review = reviewRepository.save(review);
        } catch (DataIntegrityViolationException ex) {
            // Race: a concurrent request already attached a review to this exact
            // order item (uq_product_reviews_user_order_item) - report it as the
            // same clean business rule rather than a raw constraint violation.
            throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "This purchase has already been reviewed");
        }
        return toResponse(review);
    }

    @Transactional(readOnly = true)
    public ListResult listPublic(String productIdOrSlug, ReviewListQuery query) {
        return listPublic(resolveVisibleProduct(productIdOrSlug).getId(), query);
    }

    @Transactional(readOnly = true)
    public ListResult listPublic(UUID productId, ReviewListQuery query) {
        Pageable pageable = PageRequest.of(resolvePageIndex(query.page()), resolveLimit(query.limit()));
        Page<ProductReview> page = reviewRepository.findAllByProductIdAndStatus(productId, ReviewStatus.APPROVED, pageable);
        List<ReviewResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return new ListResult(items, PageMeta.from(page));
    }

    @Transactional(readOnly = true)
    public ListResult listAdmin(ReviewListQuery query) {
        Pageable pageable = PageRequest.of(resolvePageIndex(query.page()), resolveLimit(query.limit()));
        Page<ProductReview> page = reviewRepository.findAllForAdmin(pageable);
        List<ReviewResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return new ListResult(items, PageMeta.from(page));
    }

    @Transactional(readOnly = true)
    public ReviewResponse getAdminDetail(UUID reviewId) {
        ProductReview review = reviewRepository.findDetailById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        return toResponse(review);
    }

    @Transactional
    public ReviewResponse updateStatus(UUID reviewId, UpdateReviewStatusRequest request) {
        ProductReview review = reviewRepository.findDetailById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        review.setStatus(request.status());
        return toResponse(reviewRepository.save(review));
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

    /**
     * Public review endpoints follow the same visibility contract as public
     * product detail: an ACTIVE product can be addressed by either UUID or slug,
     * and a UUID-shaped slug must still fall back to slug lookup if no ACTIVE
     * product exists by that id.
     */
    private Product resolveVisibleProduct(String productIdOrSlug) {
        UUID id = tryParseUuid(productIdOrSlug);
        if (id != null) {
            Optional<Product> byId = productRepository.findById(id)
                    .filter(p -> p.getStatus() == ProductStatus.ACTIVE);
            if (byId.isPresent()) {
                return byId.get();
            }
        }
        return productRepository.findBySlugAndStatus(productIdOrSlug, ProductStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private UUID tryParseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException notAUuid) {
            return null;
        }
    }

    private ReviewResponse toResponse(ProductReview review) {
        return new ReviewResponse(
                review.getId(),
                review.getProduct().getId(),
                review.getUser().getId(),
                review.getUser().getFullName(),
                review.getRating(),
                review.getTitle(),
                review.getContent(),
                review.getStatus(),
                review.isVerifiedPurchase(),
                review.getCreatedAt());
    }
}
