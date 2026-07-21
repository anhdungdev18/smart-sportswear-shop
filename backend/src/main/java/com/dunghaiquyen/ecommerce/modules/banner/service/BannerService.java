package com.dunghaiquyen.ecommerce.modules.banner.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.common.exception.ResourceNotFoundException;
import com.dunghaiquyen.ecommerce.common.response.PageMeta;
import com.dunghaiquyen.ecommerce.config.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import com.dunghaiquyen.ecommerce.modules.banner.dto.BannerCreateRequest;
import com.dunghaiquyen.ecommerce.modules.banner.dto.BannerItemCreateRequest;
import com.dunghaiquyen.ecommerce.modules.banner.dto.BannerItemResponse;
import com.dunghaiquyen.ecommerce.modules.banner.dto.BannerItemUpdateRequest;
import com.dunghaiquyen.ecommerce.modules.banner.dto.BannerListQuery;
import com.dunghaiquyen.ecommerce.modules.banner.dto.BannerResponse;
import com.dunghaiquyen.ecommerce.modules.banner.dto.BannerUpdateRequest;
import com.dunghaiquyen.ecommerce.modules.banner.dto.PublicBannerResponse;
import com.dunghaiquyen.ecommerce.modules.banner.entity.Banner;
import com.dunghaiquyen.ecommerce.modules.banner.entity.BannerItem;
import com.dunghaiquyen.ecommerce.modules.banner.entity.BannerPlacement;
import com.dunghaiquyen.ecommerce.modules.banner.entity.BannerStatus;
import com.dunghaiquyen.ecommerce.modules.banner.repository.BannerItemRepository;
import com.dunghaiquyen.ecommerce.modules.banner.repository.BannerRepository;
import com.dunghaiquyen.ecommerce.modules.banner.repository.spec.BannerSpecifications;
import com.dunghaiquyen.ecommerce.modules.product.entity.Product;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Admin CRUD for banners + their items, plus the public GET /api/v1/banners/active?placement= listing. */
@Service
public class BannerService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final BannerRepository bannerRepository;
    private final BannerItemRepository bannerItemRepository;
    private final ProductRepository productRepository;

    public BannerService(
            BannerRepository bannerRepository, BannerItemRepository bannerItemRepository, ProductRepository productRepository) {
        this.bannerRepository = bannerRepository;
        this.bannerItemRepository = bannerItemRepository;
        this.productRepository = productRepository;
    }

    public record ListResult(List<BannerResponse> items, PageMeta meta) {
    }

    @Cacheable(value = CacheConfig.BANNERS, key = "#placement")
    @Transactional(readOnly = true)
    public List<PublicBannerResponse> listActive(BannerPlacement placement) {
        return bannerRepository.findAllActiveByPlacement(BannerStatus.ACTIVE, placement, Instant.now()).stream()
                .map(this::toPublicResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ListResult list(BannerListQuery query) {
        Specification<Banner> spec = Specification.unrestricted();
        if (query.status() != null) {
            spec = spec.and(BannerSpecifications.hasStatus(query.status()));
        }
        if (query.placement() != null) {
            spec = spec.and(BannerSpecifications.hasPlacement(query.placement()));
        }
        Pageable pageable = PageRequest.of(
                resolvePageIndex(query.page()), resolveLimit(query.limit()), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Banner> page = bannerRepository.findAll(spec, pageable);
        List<BannerResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return new ListResult(items, PageMeta.from(page));
    }

    @Transactional(readOnly = true)
    public BannerResponse getDetail(UUID id) {
        Banner banner = bannerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Banner not found"));
        return toResponse(banner);
    }

    @CacheEvict(value = CacheConfig.BANNERS, allEntries = true)
    @Transactional
    public BannerResponse create(BannerCreateRequest request) {
        if (bannerRepository.existsByCode(request.code())) {
            throw new BusinessRuleException("Banner code already exists: " + request.code());
        }
        validateTimeRange(request.startsAt(), request.endsAt());

        Banner banner = new Banner();
        banner.setName(request.name().trim());
        banner.setCode(request.code());
        banner.setPlacement(request.placement());
        banner.setStatus(request.status() != null ? request.status() : BannerStatus.DRAFT);
        banner.setStartsAt(request.startsAt());
        banner.setEndsAt(request.endsAt());

        try {
            banner = bannerRepository.save(banner);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessRuleException("Banner code already exists: " + request.code());
        }
        return toResponse(banner);
    }

    @CacheEvict(value = CacheConfig.BANNERS, allEntries = true)
    @Transactional
    public BannerResponse update(UUID id, BannerUpdateRequest request) {
        Banner banner = bannerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Banner not found"));

        if (request.code() != null && bannerRepository.existsByCodeAndIdNot(request.code(), id)) {
            throw new BusinessRuleException("Banner code already exists: " + request.code());
        }
        Instant newStartsAt = request.startsAt() != null ? request.startsAt() : banner.getStartsAt();
        Instant newEndsAt = request.endsAt() != null ? request.endsAt() : banner.getEndsAt();
        if (request.startsAt() != null || request.endsAt() != null) {
            validateTimeRange(newStartsAt, newEndsAt);
        }

        if (request.name() != null) {
            banner.setName(request.name().trim());
        }
        if (request.code() != null) {
            banner.setCode(request.code());
        }
        if (request.placement() != null) {
            banner.setPlacement(request.placement());
        }
        if (request.status() != null) {
            banner.setStatus(request.status());
        }
        if (request.startsAt() != null) {
            banner.setStartsAt(request.startsAt());
        }
        if (request.endsAt() != null) {
            banner.setEndsAt(request.endsAt());
        }

        return toResponse(bannerRepository.save(banner));
    }

    @CacheEvict(value = CacheConfig.BANNERS, allEntries = true)
    @Transactional
    public BannerItemResponse addItem(UUID bannerId, BannerItemCreateRequest request) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new ResourceNotFoundException("Banner not found"));

        BannerItem item = new BannerItem();
        item.setBanner(banner);
        item.setTitle(request.title());
        item.setSubtitle(request.subtitle());
        item.setImageUrl(request.imageUrl());
        item.setTargetUrl(request.targetUrl());
        item.setProduct(resolveProduct(request.productId()));
        item.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        item.setActive(request.isActive() == null || request.isActive());

        return toItemResponse(bannerItemRepository.save(item));
    }

    @CacheEvict(value = CacheConfig.BANNERS, allEntries = true)
    @Transactional
    public BannerItemResponse updateItem(UUID itemId, BannerItemUpdateRequest request) {
        BannerItem item = bannerItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Banner item not found"));

        if (request.title() != null) {
            item.setTitle(request.title());
        }
        if (request.subtitle() != null) {
            item.setSubtitle(request.subtitle());
        }
        if (request.imageUrl() != null) {
            item.setImageUrl(request.imageUrl());
        }
        if (request.targetUrl() != null) {
            item.setTargetUrl(request.targetUrl());
        }
        if (request.productId() != null) {
            item.setProduct(resolveProduct(request.productId()));
        }
        if (request.sortOrder() != null) {
            item.setSortOrder(request.sortOrder());
        }
        if (request.isActive() != null) {
            item.setActive(request.isActive());
        }

        return toItemResponse(bannerItemRepository.save(item));
    }

    @CacheEvict(value = CacheConfig.BANNERS, allEntries = true)
    @Transactional
    public void deleteItem(UUID itemId) {
        if (!bannerItemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("Banner item not found");
        }
        bannerItemRepository.deleteById(itemId);
    }

    private Product resolveProduct(UUID productId) {
        if (productId == null) {
            return null;
        }
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
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
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private BannerResponse toResponse(Banner banner) {
        List<BannerItemResponse> items = bannerItemRepository.findAllByBannerIdOrderBySortOrderAsc(banner.getId())
                .stream()
                .map(this::toItemResponse)
                .toList();
        return new BannerResponse(
                banner.getId(),
                banner.getName(),
                banner.getCode(),
                banner.getPlacement(),
                banner.getStatus(),
                banner.getStartsAt(),
                banner.getEndsAt(),
                items,
                banner.getCreatedAt(),
                banner.getUpdatedAt());
    }

    private PublicBannerResponse toPublicResponse(Banner banner) {
        List<BannerItemResponse> items = bannerItemRepository.findAllByBannerIdAndIsActiveTrueOrderBySortOrderAsc(banner.getId())
                .stream()
                .map(this::toItemResponse)
                .toList();
        return new PublicBannerResponse(banner.getId(), banner.getName(), banner.getPlacement(), items);
    }

    private BannerItemResponse toItemResponse(BannerItem item) {
        return new BannerItemResponse(
                item.getId(),
                item.getBanner().getId(),
                item.getTitle(),
                item.getSubtitle(),
                item.getImageUrl(),
                item.getTargetUrl(),
                item.getProduct() != null ? item.getProduct().getId() : null,
                item.getSortOrder(),
                item.isActive());
    }
}
