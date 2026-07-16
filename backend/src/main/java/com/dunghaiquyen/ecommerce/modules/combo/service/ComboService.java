package com.dunghaiquyen.ecommerce.modules.combo.service;

import com.dunghaiquyen.ecommerce.common.exception.ResourceNotFoundException;
import com.dunghaiquyen.ecommerce.modules.combo.dto.ComboRequest;
import com.dunghaiquyen.ecommerce.modules.combo.dto.ComboResponse;
import com.dunghaiquyen.ecommerce.modules.combo.entity.Combo;
import com.dunghaiquyen.ecommerce.modules.combo.entity.ComboProduct;
import com.dunghaiquyen.ecommerce.modules.combo.entity.ComboStatus;
import com.dunghaiquyen.ecommerce.modules.combo.repository.ComboRepository;
import com.dunghaiquyen.ecommerce.modules.product.entity.Product;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Evaluates combo discounts against a cart and provides admin CRUD for combos.
 *
 * <p>A combo applies when the cart contains every product listed in the combo.
 * Each applicable combo contributes its fixed {@code discountAmount}. Multiple
 * combos can apply at once and stack. Callers clamp the final discount to the
 * order subtotal.
 */
@Service
public class ComboService {

    private final ComboRepository comboRepository;
    private final ProductRepository productRepository;

    public ComboService(ComboRepository comboRepository, ProductRepository productRepository) {
        this.comboRepository = comboRepository;
        this.productRepository = productRepository;
    }

    public record AppliedCombo(UUID comboId, String name, BigDecimal discountAmount) {
    }

    public record ComboDiscountResult(List<AppliedCombo> combos, BigDecimal totalDiscount) {
        public static ComboDiscountResult none() {
            return new ComboDiscountResult(List.of(), BigDecimal.ZERO);
        }
    }

    @Transactional(readOnly = true)
    public ComboDiscountResult calculateDiscount(Collection<UUID> cartProductIds) {
        if (cartProductIds == null || cartProductIds.isEmpty()) {
            return ComboDiscountResult.none();
        }

        Set<UUID> inCart = new HashSet<>(cartProductIds);
        List<AppliedCombo> applied = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Combo combo : comboRepository.findAllByStatusWithProducts(ComboStatus.ACTIVE)) {
            List<ComboProduct> required = combo.getProducts();
            if (required.isEmpty()) {
                continue;
            }

            boolean allPresent = required.stream()
                    .allMatch(item -> inCart.contains(item.getProduct().getId()));
            if (allPresent) {
                applied.add(new AppliedCombo(combo.getId(), combo.getName(), combo.getDiscountAmount()));
                total = total.add(combo.getDiscountAmount());
            }
        }

        return new ComboDiscountResult(applied, total);
    }

    @Transactional(readOnly = true)
    public List<ComboResponse> listAll() {
        return comboRepository.findAllWithProducts().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ComboResponse getById(UUID id) {
        return toResponse(loadOrThrow(id));
    }

    @Transactional
    public ComboResponse create(ComboRequest request) {
        Combo combo = new Combo();
        applyRequest(combo, request);
        return toResponse(comboRepository.save(combo));
    }

    @Transactional
    public ComboResponse update(UUID id, ComboRequest request) {
        Combo combo = loadOrThrow(id);
        applyRequest(combo, request);
        return toResponse(comboRepository.save(combo));
    }

    @Transactional
    public void delete(UUID id) {
        if (!comboRepository.existsById(id)) {
            throw new ResourceNotFoundException("Combo not found");
        }
        comboRepository.deleteById(id);
    }

    private Combo loadOrThrow(UUID id) {
        return comboRepository.findByIdWithProducts(id)
                .orElseThrow(() -> new ResourceNotFoundException("Combo not found"));
    }

    private void applyRequest(Combo combo, ComboRequest request) {
        combo.setName(request.name());
        combo.setDescription(request.description());
        combo.setDiscountAmount(request.discountAmount());
        combo.setStatus(request.status() != null ? request.status() : ComboStatus.ACTIVE);

        Set<UUID> requestedIds = new LinkedHashSet<>(request.productIds());
        List<Product> products = productRepository.findAllById(requestedIds);
        if (products.size() != requestedIds.size()) {
            throw new ResourceNotFoundException("One or more products in the combo were not found");
        }

        combo.getProducts().clear();
        for (Product product : products) {
            ComboProduct link = new ComboProduct();
            link.setCombo(combo);
            link.setProduct(product);
            link.setQuantity(1);
            combo.getProducts().add(link);
        }
    }

    private ComboResponse toResponse(Combo combo) {
        List<ComboResponse.ComboProductResponse> products = combo.getProducts().stream()
                .map(item -> new ComboResponse.ComboProductResponse(
                        item.getProduct().getId(), item.getProduct().getName(), item.getQuantity()))
                .toList();

        return new ComboResponse(
                combo.getId(),
                combo.getName(),
                combo.getDescription(),
                combo.getDiscountAmount(),
                combo.getStatus(),
                products,
                combo.getCreatedAt(),
                combo.getUpdatedAt());
    }
}
