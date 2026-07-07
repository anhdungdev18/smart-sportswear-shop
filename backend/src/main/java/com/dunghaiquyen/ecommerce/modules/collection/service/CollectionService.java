package com.dunghaiquyen.ecommerce.modules.collection.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.common.exception.ResourceNotFoundException;
import com.dunghaiquyen.ecommerce.common.response.PageMeta;
import com.dunghaiquyen.ecommerce.config.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import com.dunghaiquyen.ecommerce.modules.collection.dto.AddProductToCollectionRequest;
import com.dunghaiquyen.ecommerce.modules.collection.dto.CollectionCreateRequest;
import com.dunghaiquyen.ecommerce.modules.collection.dto.CollectionProductAssignmentResponse;
import com.dunghaiquyen.ecommerce.modules.collection.dto.CollectionResponse;
import com.dunghaiquyen.ecommerce.modules.collection.dto.CollectionUpdateRequest;
import com.dunghaiquyen.ecommerce.modules.collection.dto.PublicCollectionDetailResponse;
import com.dunghaiquyen.ecommerce.modules.collection.dto.PublicCollectionResponse;
import com.dunghaiquyen.ecommerce.modules.collection.entity.Collection;
import com.dunghaiquyen.ecommerce.modules.collection.entity.CollectionStatus;
import com.dunghaiquyen.ecommerce.modules.collection.entity.ProductCollection;
import com.dunghaiquyen.ecommerce.modules.collection.repository.CollectionRepository;
import com.dunghaiquyen.ecommerce.modules.collection.repository.ProductCollectionRepository;
import com.dunghaiquyen.ecommerce.modules.product.dto.ProductListItemResponse;
import com.dunghaiquyen.ecommerce.modules.product.entity.Product;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductStatus;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductRepository;
import com.dunghaiquyen.ecommerce.modules.product.service.ProductService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CollectionService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final CollectionRepository collectionRepository;
    private final ProductCollectionRepository productCollectionRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public CollectionService(
            CollectionRepository collectionRepository,
            ProductCollectionRepository productCollectionRepository,
            ProductRepository productRepository,
            ProductService productService) {
        this.collectionRepository = collectionRepository;
        this.productCollectionRepository = productCollectionRepository;
        this.productRepository = productRepository;
        this.productService = productService;
    }

    public record AdminListResult(List<CollectionResponse> items, PageMeta meta) {
    }

    // ===== PUBLIC ===========================================================

    @Cacheable(CacheConfig.COLLECTIONS)
    @Transactional(readOnly = true)
    public List<PublicCollectionResponse> listPublic() {
        return collectionRepository.findAllActive(CollectionStatus.ACTIVE, Instant.now()).stream()
                .map(this::toPublicResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicCollectionDetailResponse getPublicDetail(String slug) {
        Collection collection = collectionRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

        // Guard: only ACTIVE + currently within time window
        if (!isPubliclyVisible(collection)) {
            throw new ResourceNotFoundException("Collection not found");
        }

        List<ProductListItemResponse> products = assembleCollectionProducts(collection.getId());

        return new PublicCollectionDetailResponse(
                collection.getId(),
                collection.getName(),
                collection.getSlug(),
                collection.getDescription(),
                collection.getShortDescription(),
                collection.getCollectionType(),
                collection.getSeason(),
                collection.getYear(),
                collection.getBannerImageUrl(),
                collection.getCoverImageUrl(),
                collection.getSortOrder(),
                collection.isFeatured(),
                products);
    }

    // ===== ADMIN ============================================================

    @Transactional(readOnly = true)
    public AdminListResult listAdmin(Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(
                resolvePageIndex(page), resolveLimit(limit),
                Sort.by(Sort.Direction.ASC, "sortOrder").and(Sort.by(Sort.Direction.DESC, "createdAt")));
        Page<Collection> p = collectionRepository.findAll(pageable);
        return new AdminListResult(p.getContent().stream().map(this::toAdminResponse).toList(), PageMeta.from(p));
    }

    @Transactional(readOnly = true)
    public List<CollectionProductAssignmentResponse> listAssignedProducts(UUID collectionId) {
        if (!collectionRepository.existsById(collectionId)) {
            throw new ResourceNotFoundException("Collection not found");
        }

        return productCollectionRepository.findAllByCollectionIdOrderBySortOrderAsc(collectionId).stream()
                .map(ProductCollection::getProduct)
                .map(product -> new CollectionProductAssignmentResponse(
                        product.getId(),
                        product.getName(),
                        product.getSlug(),
                        product.getStatus().name()))
                .toList();
    }

    @CacheEvict(value = CacheConfig.COLLECTIONS, allEntries = true)
    @Transactional
    public CollectionResponse create(CollectionCreateRequest request) {
        if (collectionRepository.existsBySlug(request.slug())) {
            throw new BusinessRuleException("Slug already exists: " + request.slug());
        }
        validateTimeRange(request.startsAt(), request.endsAt());

        Collection c = new Collection();
        c.setName(request.name().trim());
        c.setSlug(request.slug());
        c.setDescription(request.description());
        c.setShortDescription(request.shortDescription());
        c.setCollectionType(request.collectionType());
        c.setSeason(request.season());
        c.setYear(request.year());
        c.setBannerImageUrl(request.bannerImageUrl());
        c.setCoverImageUrl(request.coverImageUrl());
        c.setStatus(request.status() != null ? request.status() : CollectionStatus.DRAFT);
        c.setStartsAt(request.startsAt());
        c.setEndsAt(request.endsAt());
        c.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        c.setFeatured(request.isFeatured() != null && request.isFeatured());

        try {
            c = collectionRepository.save(c);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessRuleException("Slug already exists: " + request.slug());
        }
        return toAdminResponse(c);
    }

    @CacheEvict(value = CacheConfig.COLLECTIONS, allEntries = true)
    @Transactional
    public CollectionResponse update(UUID id, CollectionUpdateRequest request) {
        Collection c = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

        if (request.slug() != null && collectionRepository.existsBySlugAndIdNot(request.slug(), id)) {
            throw new BusinessRuleException("Slug already exists: " + request.slug());
        }

        Instant newStartsAt = request.startsAt() != null ? request.startsAt() : c.getStartsAt();
        Instant newEndsAt = request.endsAt() != null ? request.endsAt() : c.getEndsAt();
        if (request.startsAt() != null || request.endsAt() != null) {
            validateTimeRange(newStartsAt, newEndsAt);
        }

        if (request.name() != null) c.setName(request.name().trim());
        if (request.slug() != null) c.setSlug(request.slug());
        if (request.description() != null) c.setDescription(request.description());
        if (request.shortDescription() != null) c.setShortDescription(request.shortDescription());
        if (request.collectionType() != null) c.setCollectionType(request.collectionType());
        if (request.season() != null) c.setSeason(request.season());
        if (request.year() != null) c.setYear(request.year());
        if (request.bannerImageUrl() != null) c.setBannerImageUrl(request.bannerImageUrl());
        if (request.coverImageUrl() != null) c.setCoverImageUrl(request.coverImageUrl());
        if (request.status() != null) c.setStatus(request.status());
        if (request.startsAt() != null) c.setStartsAt(request.startsAt());
        if (request.endsAt() != null) c.setEndsAt(request.endsAt());
        if (request.sortOrder() != null) c.setSortOrder(request.sortOrder());
        if (request.isFeatured() != null) c.setFeatured(request.isFeatured());

        return toAdminResponse(collectionRepository.save(c));
    }

    /**
     * Links a product to a collection. The (product_id, collection_id) uniqueness
     * is enforced at the DB level (uq_product_collections in V10) - duplicate
     * inserts surface as DataIntegrityViolationException and are mapped to a
     * clean 409 rather than a generic 500. This makes FE idempotent-safe: a
     * double-submit produces a clear business error, not an unhandled exception.
     */
    @CacheEvict(value = CacheConfig.COLLECTIONS, allEntries = true)
    @Transactional
    public void linkProduct(UUID productId, AddProductToCollectionRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        Collection collection = collectionRepository.findById(request.collectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

        ProductCollection link = new ProductCollection();
        link.setProduct(product);
        link.setCollection(collection);
        link.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        link.setPrimary(request.isPrimary() != null && request.isPrimary());

        try {
            // saveAndFlush (not plain save) forces Hibernate to flush the INSERT immediately
            // so that the unique-constraint violation surfaces HERE, inside the catch block,
            // rather than at transaction-commit time (after this method returns) where it
            // would escape as an unhandled 500. Same pattern already used for order_code
            // collision retry in OrderService.
            productCollectionRepository.saveAndFlush(link);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessRuleException(HttpStatus.CONFLICT, "Product is already in this collection");
        }
    }

    /**
     * Removes a product-collection link. Returns silently if the link does not
     * exist (idempotent DELETE: the desired post-condition "product is not in
     * collection" is already satisfied whether the row existed or not).
     * Uses deleteById rather than delete(entity) to avoid triggering lazy-load
     * of product/collection associations that are not needed for a plain DELETE.
     */
    @Transactional
    @CacheEvict(value = CacheConfig.COLLECTIONS, allEntries = true)
    public void unlinkProduct(UUID productId, UUID collectionId) {
        productCollectionRepository.findByProductIdAndCollectionId(productId, collectionId)
                .ifPresent(pc -> productCollectionRepository.deleteById(pc.getId()));
    }

    // ===== HELPERS ==========================================================

    private boolean isPubliclyVisible(Collection c) {
        if (c.getStatus() != CollectionStatus.ACTIVE) {
            return false;
        }
        Instant now = Instant.now();
        if (c.getStartsAt() != null && now.isBefore(c.getStartsAt())) {
            return false;
        }
        if (c.getEndsAt() != null && now.isAfter(c.getEndsAt())) {
            return false;
        }
        return true;
    }

    /**
     * Fetches the products belonging to a collection, in sort_order order,
     * only ACTIVE ones so a product that was unpublished doesn't show up
     * on the collection landing page. Delegates to ProductService.assembleListItemsById
     * to reuse the same thumbnail + price assembly logic already there.
     */
    private List<ProductListItemResponse> assembleCollectionProducts(UUID collectionId) {
        List<UUID> productIds = productCollectionRepository
                .findAllByCollectionIdOrderBySortOrderAsc(collectionId)
                .stream()
                .map(pc -> pc.getProduct().getId())
                .toList();
        if (productIds.isEmpty()) {
            return List.of();
        }
        // Only surface ACTIVE products even if the collection membership still exists.
        return productService.assembleListItemsByIds(productIds, ProductStatus.ACTIVE);
    }

    private void validateTimeRange(Instant startsAt, Instant endsAt) {
        if (startsAt != null && endsAt != null && startsAt.isAfter(endsAt)) {
            throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "startsAt must not be after endsAt");
        }
    }

    private int resolvePageIndex(Integer page) {
        return (page != null && page > 0) ? page - 1 : 0;
    }

    private int resolveLimit(Integer limit) {
        if (limit == null || limit <= 0) return DEFAULT_LIMIT;
        return Math.min(limit, MAX_LIMIT);
    }

    private CollectionResponse toAdminResponse(Collection c) {
        return new CollectionResponse(
                c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getShortDescription(),
                c.getCollectionType(), c.getSeason(), c.getYear(), c.getBannerImageUrl(), c.getCoverImageUrl(),
                c.getStatus(), c.getStartsAt(), c.getEndsAt(), c.getSortOrder(), c.isFeatured(),
                c.getCreatedAt(), c.getUpdatedAt());
    }

    private PublicCollectionResponse toPublicResponse(Collection c) {
        return new PublicCollectionResponse(
                c.getId(), c.getName(), c.getSlug(), c.getShortDescription(),
                c.getCollectionType(), c.getSeason(), c.getYear(), c.getBannerImageUrl(), c.getCoverImageUrl(),
                c.getSortOrder(), c.isFeatured());
    }
}
