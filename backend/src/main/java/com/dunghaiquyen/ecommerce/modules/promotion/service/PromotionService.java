package com.dunghaiquyen.ecommerce.modules.promotion.service;

import com.dunghaiquyen.ecommerce.common.exception.ResourceNotFoundException;
import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.modules.promotion.dto.ActivePromotionResponse;
import com.dunghaiquyen.ecommerce.modules.promotion.dto.PromotionCreateRequest;
import com.dunghaiquyen.ecommerce.modules.promotion.dto.PromotionResponse;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.Promotion;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionProduct;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionScope;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionStatus;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionType;
import com.dunghaiquyen.ecommerce.modules.promotion.repository.ProductPromoDiscount;
import com.dunghaiquyen.ecommerce.modules.promotion.repository.PromotionProductRepository;
import com.dunghaiquyen.ecommerce.modules.promotion.repository.PromotionRepository;
import java.text.Normalizer;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionProductRepository promotionProductRepository;

    public PromotionService(
            PromotionRepository promotionRepository,
            PromotionProductRepository promotionProductRepository) {
        this.promotionRepository = promotionRepository;
        this.promotionProductRepository = promotionProductRepository;
    }

    /**
     * Best active percentage discount per product (empty when nothing applies).
     * Called by ProductService when assembling list items so promo prices flow
     * everywhere product listings are shown.
     */
    @Transactional(readOnly = true)
    public Map<UUID, Integer> activePercentDiscountByProduct(Collection<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        return promotionRepository.findActivePercentDiscounts(Instant.now(), productIds).stream()
                .filter(d -> d.getPercent() != null)
                .collect(Collectors.toMap(
                        ProductPromoDiscount::getProductId,
                        d -> d.getPercent().intValue(),
                        Math::max));
    }

    /** Product ids currently discounted by an active promotion (used by the discount=any filter). */
    @Transactional(readOnly = true)
    public List<UUID> activePromotionProductIds() {
        return promotionRepository.findActivePromotionProductIds(Instant.now());
    }

    @Transactional(readOnly = true)
    public List<ActivePromotionResponse> listActive() {
        return promotionRepository.findActiveProductPromotions(Instant.now()).stream()
                .map(p -> new ActivePromotionResponse(
                        p.getId(), p.getName(), p.getSlug(), p.getDiscountPercent(),
                        p.getStartsAt(), p.getEndsAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> listAll() {
        return promotionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PromotionResponse create(PromotionCreateRequest req, UUID adminId) {
        validateWindow(req.startsAt(), req.endsAt());
        Promotion promotion = new Promotion();
        promotion.setName(req.name().trim());
        promotion.setSlug(uniqueSlug(req.name()));
        promotion.setDescription(req.description());
        promotion.setType(PromotionType.PERCENTAGE);
        promotion.setScope(PromotionScope.PRODUCT);
        promotion.setDiscountPercent(req.discountPercent());
        promotion.setStartsAt(req.startsAt());
        promotion.setEndsAt(req.endsAt());
        promotion.setStatus(PromotionStatus.ACTIVE);
        promotion.setCreatedBy(adminId);
        Promotion saved = promotionRepository.save(promotion);

        for (UUID productId : req.productIds().stream().distinct().toList()) {
            PromotionProduct link = new PromotionProduct();
            link.setPromotionId(saved.getId());
            link.setProductId(productId);
            promotionProductRepository.save(link);
        }
        return toResponse(saved);
    }

    @Transactional
    public PromotionResponse update(UUID id, PromotionCreateRequest req) {
        validateWindow(req.startsAt(), req.endsAt());
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));

        promotion.setName(req.name().trim());
        promotion.setDescription(req.description());
        promotion.setDiscountPercent(req.discountPercent());
        promotion.setStartsAt(req.startsAt());
        promotion.setEndsAt(req.endsAt());
        Promotion saved = promotionRepository.save(promotion);

        promotionProductRepository.deleteByPromotionId(id);
        for (UUID productId : req.productIds().stream().distinct().toList()) {
            PromotionProduct link = new PromotionProduct();
            link.setPromotionId(saved.getId());
            link.setProductId(productId);
            promotionProductRepository.save(link);
        }
        return toResponse(saved);
    }

    @Transactional
    public PromotionResponse updateStatus(UUID id, PromotionStatus status) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
        promotion.setStatus(status);
        return toResponse(promotionRepository.save(promotion));
    }

    @Transactional
    public void delete(UUID id) {
        if (!promotionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Promotion not found");
        }
        promotionProductRepository.deleteByPromotionId(id);
        promotionRepository.deleteById(id);
    }

    private PromotionResponse toResponse(Promotion p) {
        List<UUID> productIds = promotionProductRepository.findByPromotionId(p.getId()).stream()
                .map(PromotionProduct::getProductId)
                .toList();
        Instant now = Instant.now();
        boolean live = p.getStatus() == PromotionStatus.ACTIVE
                && (p.getStartsAt() == null || !p.getStartsAt().isAfter(now))
                && (p.getEndsAt() == null || !p.getEndsAt().isBefore(now));
        return new PromotionResponse(
                p.getId(), p.getName(), p.getSlug(), p.getDescription(), p.getDiscountPercent(),
                p.getStartsAt(), p.getEndsAt(), p.getStatus(), live, productIds.size(), productIds);
    }

    private void validateWindow(Instant startsAt, Instant endsAt) {
        if (startsAt != null && endsAt != null && !endsAt.isAfter(startsAt)) {
            throw new BusinessRuleException("Promotion end time must be after start time");
        }
    }

    private String uniqueSlug(String name) {
        String base = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (base.isBlank()) {
            base = "km";
        }
        String slug = base;
        while (promotionRepository.existsBySlug(slug)) {
            slug = base + "-" + UUID.randomUUID().toString().substring(0, 6);
        }
        return slug;
    }
}
