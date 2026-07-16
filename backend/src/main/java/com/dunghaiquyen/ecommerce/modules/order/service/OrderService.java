package com.dunghaiquyen.ecommerce.modules.order.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.common.exception.ResourceNotFoundException;
import com.dunghaiquyen.ecommerce.common.response.PageMeta;
import com.dunghaiquyen.ecommerce.common.time.AppTimeZone;
import com.dunghaiquyen.ecommerce.config.AppShippingProperties;
import com.dunghaiquyen.ecommerce.modules.address.entity.Address;
import com.dunghaiquyen.ecommerce.modules.address.repository.AddressRepository;
import com.dunghaiquyen.ecommerce.modules.cart.entity.Cart;
import com.dunghaiquyen.ecommerce.modules.cart.entity.CartItem;
import com.dunghaiquyen.ecommerce.modules.cart.repository.CartItemRepository;
import com.dunghaiquyen.ecommerce.modules.cart.repository.CartRepository;
import com.dunghaiquyen.ecommerce.modules.combo.service.ComboService;
import com.dunghaiquyen.ecommerce.modules.inventory.service.InventoryService;
import com.dunghaiquyen.ecommerce.modules.notification.service.NotificationService;
import com.dunghaiquyen.ecommerce.modules.order.dto.AdminOrderListQuery;
import com.dunghaiquyen.ecommerce.modules.order.dto.AdminOrderResponse;
import com.dunghaiquyen.ecommerce.modules.order.dto.CreateOrderRequest;
import com.dunghaiquyen.ecommerce.modules.order.dto.OrderItemResponse;
import com.dunghaiquyen.ecommerce.modules.order.dto.OrderListQuery;
import com.dunghaiquyen.ecommerce.modules.order.dto.OrderResponse;
import com.dunghaiquyen.ecommerce.modules.order.dto.UpdateOrderStatusRequest;
import com.dunghaiquyen.ecommerce.modules.order.entity.Order;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderItem;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import com.dunghaiquyen.ecommerce.modules.order.mapper.OrderMapper;
import com.dunghaiquyen.ecommerce.modules.order.repository.OrderItemRepository;
import com.dunghaiquyen.ecommerce.modules.order.repository.OrderRepository;
import com.dunghaiquyen.ecommerce.modules.order.repository.spec.OrderSpecifications;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductStatus;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductVariant;
import com.dunghaiquyen.ecommerce.modules.product.entity.VariantStatus;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductVariantRepository;
import com.dunghaiquyen.ecommerce.modules.user.entity.User;
import com.dunghaiquyen.ecommerce.modules.user.entity.UserRole;
import com.dunghaiquyen.ecommerce.modules.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    /**
     * Deliberately linear, not a general graph: every step here is the one
     * concrete progression the spec describes (confirm -> pack -> ship ->
     * deliver), plus a single early exit to CANCELLED. Cancelling after
     * CONFIRMED/PACKING/SHIPPING is NOT reachable through this matrix - spec
     * only describes releasing reserved stock on cancel, never restocking
     * already-deducted stock, so a "real" cancel past CONFIRMED would need a
     * return/restock flow this phase does not define. Documented tradeoff,
     * not an oversight.
     */
    private static final Map<OrderStatus, java.util.Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING_CONFIRMATION,
                    java.util.Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, java.util.Set.of(OrderStatus.PACKING),
            OrderStatus.PACKING, java.util.Set.of(OrderStatus.SHIPPING),
            OrderStatus.SHIPPING, java.util.Set.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, java.util.Set.of(),
            OrderStatus.CANCELLED, java.util.Set.of());

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final OrderMapper orderMapper;
    private final ComboService comboService;
    private final NotificationService notificationService;
    private final AppShippingProperties shippingProperties;

    public OrderService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            AddressRepository addressRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductVariantRepository variantRepository,
            UserRepository userRepository,
            InventoryService inventoryService,
            OrderMapper orderMapper,
            ComboService comboService,
            NotificationService notificationService,
            AppShippingProperties shippingProperties) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.addressRepository = addressRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.variantRepository = variantRepository;
        this.userRepository = userRepository;
        this.inventoryService = inventoryService;
        this.orderMapper = orderMapper;
        this.comboService = comboService;
        this.notificationService = notificationService;
        this.shippingProperties = shippingProperties;
    }

    public record ListResult<T>(List<T> items, PageMeta meta) {
    }

    /**
     * Re-validates every cart line against CURRENT variant/product state
     * (never trusts anything the client might already have cached from an
     * earlier cart read) before writing anything, then creates the order,
     * reserves stock and clears the cart - all in one transaction so a
     * failure at any point leaves no order, no stock change, and no cart
     * mutation behind.
     */
    @Transactional
    public OrderResponse createOrderFromCart(UUID userId, CreateOrderRequest request) {
        // Locked, not a plain read: two concurrent checkout requests for the same
        // user/cart would otherwise both read the same non-empty cart items before
        // either clears them, and (if stock allows) both create an order from the
        // same cart. The second request blocks here until the first commits, then
        // re-reads cart items fresh below and finds them already gone.
        Cart cart = cartRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Cart is empty"));
        List<CartItem> cartItems = cartItemRepository.findAllByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Cart is empty");
        }

        Address address = addressRepository.findById(request.addressId())
                .filter(a -> a.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // Phase 1: lock + validate every line BEFORE writing anything. Sorted by
        // variant id so concurrent checkouts/admin actions always acquire row
        // locks in the same order - avoids a classic deadlock where two
        // transactions lock the same two rows in opposite order.
        List<CartItem> sortedItems = cartItems.stream()
                .sorted(Comparator.comparing(ci -> ci.getVariant().getId()))
                .toList();

        List<ValidatedLine> validated = new ArrayList<>();
        for (CartItem cartItem : sortedItems) {
            LineCheck check = checkLine(cartItem, true);
            if (!check.isValid()) {
                if (check.errorStatus() == HttpStatus.NOT_FOUND) {
                    throw new ResourceNotFoundException(check.errorMessage());
                }
                throw new BusinessRuleException(check.errorStatus(), check.errorMessage());
            }
            validated.add(new ValidatedLine(cartItem.getQuantity(), check.variant()));
        }

        // Phase 2: build the order using prices read just now, under lock - never
        // anything cached from an earlier cart response.
        User user = userRepository.getReferenceById(userId);
        Order order = new Order();
        order.setUser(user);
        order.setOrderCode(generateOrderCode());
        order.setAddressSnapshotJson(buildAddressSnapshot(address));
        order.setPaymentMethod(request.paymentMethod());
        order.setNote(request.note());

        BigDecimal subtotal = BigDecimal.ZERO;
        for (ValidatedLine line : validated) {
            ProductVariant variant = line.variant();
            BigDecimal lineTotal = variant.getPrice().multiply(BigDecimal.valueOf(line.quantity()));
            subtotal = subtotal.add(lineTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(variant.getProduct());
            orderItem.setVariant(variant);
            orderItem.setProductNameSnapshot(variant.getProduct().getName());
            orderItem.setSkuSnapshot(variant.getSku());
            orderItem.setSizeSnapshot(variant.getSize());
            orderItem.setColorSnapshot(variant.getColor());
            orderItem.setUnitPriceSnapshot(variant.getPrice());
            orderItem.setQuantity(line.quantity());
            orderItem.setLineTotal(lineTotal);
            order.getItems().add(orderItem);
        }

        order.setSubtotalAmount(subtotal);
        // See calculateShippingFee's javadoc - also used by CheckoutPreviewService,
        // so preview and the real order always agree on this number.
        order.setShippingFee(calculateShippingFee(subtotal));

        // Combo (bundle) discount: every active combo whose full product set is
        // present in this cart takes its flat amount off. Computed BEFORE the
        // order is written, same "decide the final numbers before persisting"
        // discipline as the stock checks above. Clamped to the subtotal so the
        // total can never go negative.
        java.util.Set<UUID> comboProductIds = order.getItems().stream()
                .map(item -> item.getProduct().getId())
                .collect(java.util.stream.Collectors.toSet());
        BigDecimal discount = comboService.calculateDiscount(comboProductIds).totalDiscount().min(subtotal);
        order.setDiscountAmount(discount);
        order.setTotalAmount(subtotal.add(order.getShippingFee()).subtract(discount));

        try {
            // saveAndFlush, not save: a plain save() only enqueues the INSERT in
            // Hibernate's session - without a flush, the order_code unique
            // constraint is not actually checked by Postgres until SOME LATER
            // flush point (e.g. the SELECT inside assembleResponse below), by
            // which time this catch block has already been skipped over and the
            // violation surfaces as an unhandled 500 instead of triggering the
            // retry it was meant to. Incidental fix - this collision-retry was
            // already silently ineffective; only caught now because the test
            // suite has grown enough to occasionally hit a true 5-digit
            // order_code collision within the same day's order volume.
            order = orderRepository.saveAndFlush(order);
        } catch (DataIntegrityViolationException ex) {
            // order_code collision (vanishingly unlikely with date+random suffix) -
            // retry once with a freshly generated code rather than fail checkout.
            order.setOrderCode(generateOrderCode());
            order = orderRepository.saveAndFlush(order);
        }

        // Phase 3: reserve stock for every line, reusing the SAME locked variant
        // entities from Phase 1 (still locked for the rest of this transaction).
        for (ValidatedLine line : validated) {
            inventoryService.recordReserve(line.variant(), line.quantity(), order, user);
        }

        // Phase 4: the cart that was just turned into an order must not still
        // offer the same items for a second checkout.
        cartItemRepository.deleteAll(cartItems);

        // Phase O: confirmation email. Runs inside this same transaction and
        // never throws (see NotificationService's class javadoc) - a failed
        // send is recorded but must not undo the order that was just created.
        notificationService.notifyOrderCreated(order);

        return assembleResponse(order);
    }

    @Transactional(readOnly = true)
    public ListResult<OrderResponse> listMyOrders(UUID userId, OrderListQuery query) {
        Specification<Order> spec = OrderSpecifications.belongsToUser(userId);
        if (query.status() != null) {
            spec = spec.and(OrderSpecifications.hasStatus(query.status()));
        }
        Pageable pageable = PageRequest.of(
                resolvePageIndex(query.page()), resolveLimit(query.limit()), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> page = orderRepository.findAll(spec, pageable);
        List<OrderResponse> items = assembleResponses(page.getContent());
        return new ListResult<>(items, PageMeta.from(page));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(UUID orderId, UUID callerId, UserRole callerRole) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (callerRole == UserRole.CUSTOMER && !order.getUser().getId().equals(callerId)) {
            throw new ResourceNotFoundException("Order not found");
        }
        return assembleResponse(order);
    }

    @Transactional(readOnly = true)
    public ListResult<AdminOrderResponse> listOrdersForAdmin(AdminOrderListQuery query) {
        Specification<Order> spec = OrderSpecifications.fetchUser();
        if (query.status() != null) {
            spec = spec.and(OrderSpecifications.hasStatus(query.status()));
        }
        if (query.paymentStatus() != null) {
            spec = spec.and(OrderSpecifications.hasPaymentStatus(query.paymentStatus()));
        }
        if (query.paymentMethod() != null) {
            spec = spec.and(OrderSpecifications.hasPaymentMethod(query.paymentMethod()));
        }
        if (query.keyword() != null && !query.keyword().isBlank()) {
            spec = spec.and(OrderSpecifications.keywordMatches(query.keyword().trim()));
        }
        if (query.dateFrom() != null) {
            spec = spec.and(OrderSpecifications.createdFrom(
                    query.dateFrom().atStartOfDay(AppTimeZone.ZONE).toInstant()));
        }
        if (query.dateTo() != null) {
            spec = spec.and(OrderSpecifications.createdTo(
                    LocalDateTime.of(query.dateTo(), java.time.LocalTime.MAX).atZone(AppTimeZone.ZONE).toInstant()));
        }
        Pageable pageable = PageRequest.of(
                resolvePageIndex(query.page()), resolveLimit(query.limit()), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> page = orderRepository.findAll(spec, pageable);
        List<AdminOrderResponse> items = assembleAdminResponses(page.getContent());
        return new ListResult<>(items, PageMeta.from(page));
    }

    @Transactional(readOnly = true)
    public AdminOrderResponse getOrderDetailForAdmin(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return assembleAdminResponse(order);
    }

    @Transactional
    public AdminOrderResponse updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request, User actor) {
        // Locked, not a plain read: two concurrent status-update calls for the
        // same order (admin double-click, or admin-confirm racing a
        // customer-cancel) could otherwise both read the same PENDING_CONFIRMATION
        // snapshot and both pass the transition check, double-deducting or
        // double-releasing stock. The second call blocks here until the first
        // commits, then re-reads the now-advanced status and is correctly
        // rejected by applyStatusTransition's ALLOWED_TRANSITIONS check.
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order = applyStatusTransition(order, request.status(), actor);
        if (request.note() != null) {
            order.setInternalNote(request.note());
            order = orderRepository.save(order);
        }
        return assembleAdminResponse(order);
    }

    /**
     * Customer-initiated cancel (API_SPEC_PHASE1.md 7.4 / TASK_BREAKDOWN_PHASE1.md
     * G4). Deliberately reuses {@link #applyStatusTransition} rather than its own
     * rule set: ALLOWED_TRANSITIONS already only permits CANCELLED from
     * PENDING_CONFIRMATION, which is also the only point where stock is still
     * just "reserved" (not yet deducted) - matching the explicit decision to keep
     * customer self-cancel out of the CONFIRMED/PACKING restock territory rather
     * than introduce a second, inconsistent cancellation rule alongside admin's.
     * "Chưa SHIPPING" from the spec is satisfied by this stricter bound; it just
     * never reaches CONFIRMED/PACKING because those have already deducted real
     * stock, which only a return/restock flow (out of scope) could undo safely.
     */
    @Transactional
    public OrderResponse cancelOwnOrder(UUID orderId, UUID customerId, String reason) {
        // Same race-prevention reasoning as updateOrderStatus - lock before
        // reading the status this decision depends on.
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUser().getId().equals(customerId)) {
            throw new ResourceNotFoundException("Order not found");
        }
        User actor = order.getUser();
        order = applyStatusTransition(order, OrderStatus.CANCELLED, actor);
        if (reason != null && !reason.isBlank()) {
            // No dedicated "cancel reason" column exists (no migration warranted for
            // this patch) - internalNote is staff-visible metadata about the order's
            // lifecycle, which is exactly what this is, and customer-facing `note`
            // must not be overwritten (it holds whatever the customer set at checkout).
            order.setInternalNote("Cancelled by customer: " + reason.trim());
            order = orderRepository.save(order);
        }
        return assembleResponse(order);
    }

    /**
     * Shared by admin status updates and customer self-cancel: validates the
     * transition against ALLOWED_TRANSITIONS, applies the matching inventory
     * side effect (deduct on CONFIRMED, release on CANCELLED), and persists the
     * new status. Both callers must see the exact same rule set - splitting this
     * into two independently-maintained checks would risk them silently
     * diverging (e.g. one path allowing a cancel the other forbids).
     */
    private Order applyStatusTransition(Order order, OrderStatus target, User actor) {
        OrderStatus current = order.getOrderStatus();
        if (!ALLOWED_TRANSITIONS.getOrDefault(current, java.util.Set.of()).contains(target)) {
            throw new BusinessRuleException(
                    HttpStatus.CONFLICT, "Cannot transition order from " + current + " to " + target);
        }

        List<OrderItem> items = orderItemRepository.findAllByOrderIdOrderByIdAsc(order.getId());
        // Lock variants in a fixed order before mutating, same deadlock-avoidance
        // reasoning as order creation.
        List<OrderItem> sortedItems = items.stream()
                .sorted(Comparator.comparing(i -> i.getVariant().getId()))
                .toList();

        if (target == OrderStatus.CONFIRMED) {
            for (OrderItem item : sortedItems) {
                inventoryService.confirmDeduct(item.getVariant().getId(), item.getQuantity(), order, actor);
            }
        } else if (target == OrderStatus.CANCELLED) {
            for (OrderItem item : sortedItems) {
                inventoryService.release(item.getVariant().getId(), item.getQuantity(), order, actor);
            }
        }

        order.setOrderStatus(target);
        if (target == OrderStatus.DELIVERED) {
            // Returns module's return-window eligibility check reads this - see Order.deliveredAt's javadoc.
            order.setDeliveredAt(java.time.Instant.now());
        }
        order = orderRepository.save(order);

        // Phase O: fires for CANCELLED regardless of who triggered it (admin or
        // the customer themselves, both reach this shared method) - the order's
        // own customer should hear about a cancellation either way.
        if (target == OrderStatus.CANCELLED) {
            notificationService.notifyOrderCancelled(order);
        } else if (target == OrderStatus.DELIVERED) {
            notificationService.notifyOrderDelivered(order);
        }
        return order;
    }

    private record ValidatedLine(int quantity, ProductVariant variant) {
    }

    /**
     * Single line-level result shared by createOrderFromCart (lockForUpdate=true,
     * throws on the first invalid line) and checkCartLines (lockForUpdate=false,
     * collects every line's verdict) - the exact same rules (variant exists,
     * ACTIVE product/variant, enough stock) back both call sites, which is the
     * whole point: a checkout preview must never say "fine" about something the
     * real checkout would then reject, or vice versa.
     */
    private record LineCheck(
            CartItem cartItem, ProductVariant variant, BigDecimal lineTotal, HttpStatus errorStatus, String errorMessage) {
        boolean isValid() {
            return errorMessage == null;
        }
    }

    private LineCheck checkLine(CartItem cartItem, boolean lockForUpdate) {
        var variantOpt = lockForUpdate
                ? variantRepository.findByIdForUpdate(cartItem.getVariant().getId())
                : variantRepository.findById(cartItem.getVariant().getId());
        if (variantOpt.isEmpty()) {
            return new LineCheck(cartItem, null, null, HttpStatus.NOT_FOUND, "Variant not found");
        }
        ProductVariant variant = variantOpt.get();
        if (variant.getStatus() == VariantStatus.INACTIVE) {
            return new LineCheck(
                    cartItem, variant, null, HttpStatus.UNPROCESSABLE_ENTITY,
                    "Variant " + variant.getSku() + " is no longer available");
        }
        if (variant.getProduct().getStatus() != ProductStatus.ACTIVE) {
            return new LineCheck(
                    cartItem, variant, null, HttpStatus.UNPROCESSABLE_ENTITY,
                    "Product " + variant.getProduct().getName() + " is no longer available");
        }
        int available = variant.getStockQuantity() - variant.getReservedQuantity();
        if (cartItem.getQuantity() > available) {
            return new LineCheck(
                    cartItem, variant, null, HttpStatus.UNPROCESSABLE_ENTITY, "Insufficient stock for " + variant.getSku());
        }
        BigDecimal lineTotal = variant.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        return new LineCheck(cartItem, variant, lineTotal, null, null);
    }

    /** One cart line's checkout-preview verdict - public/DTO-safe (no entity references), used by CheckoutPreviewService. */
    public record CartLineCheck(
            UUID variantId,
            UUID productId,
            String productName,
            String sku,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal,
            boolean valid,
            String errorMessage) {
    }

    public record CartLinesCheckResult(List<CartLineCheck> lines) {
        public boolean allValid() {
            return lines.stream().allMatch(CartLineCheck::valid);
        }

        public BigDecimal validSubtotal() {
            return lines.stream()
                    .filter(CartLineCheck::valid)
                    .map(CartLineCheck::lineTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    /**
     * Read-only counterpart of createOrderFromCart's Phase 1 validation loop -
     * deliberately does NOT pessimistic-lock any variant row (lockForUpdate=false
     * in checkLine): a preview is, by definition, not about to write anything,
     * and holding write locks for a "just looking" call would only contend with
     * real checkouts for no benefit. Used by CheckoutPreviewService.
     */
    @Transactional(readOnly = true)
    public CartLinesCheckResult checkCartLines(UUID userId) {
        var cartOpt = cartRepository.findByUserId(userId);
        if (cartOpt.isEmpty()) {
            return new CartLinesCheckResult(List.of());
        }
        List<CartItem> items = cartItemRepository.findAllByCartId(cartOpt.get().getId());
        List<CartLineCheck> checks = items.stream().map(ci -> toCartLineCheck(ci, checkLine(ci, false))).toList();
        return new CartLinesCheckResult(checks);
    }

    private CartLineCheck toCartLineCheck(CartItem cartItem, LineCheck check) {
        ProductVariant variant = check.variant();
        UUID variantId = cartItem.getVariant().getId();
        UUID productId = variant != null ? variant.getProduct().getId() : null;
        String productName = variant != null ? variant.getProduct().getName() : null;
        String sku = variant != null ? variant.getSku() : null;
        BigDecimal unitPrice = variant != null ? variant.getPrice() : null;
        BigDecimal lineTotal = check.isValid() ? check.lineTotal() : BigDecimal.ZERO;
        return new CartLineCheck(
                variantId, productId, productName, sku, cartItem.getQuantity(), unitPrice, lineTotal,
                check.isValid(), check.errorMessage());
    }

    /**
     * No real shipping engine exists this phase (AppShippingProperties' javadoc) -
     * flat fee, waived once subtotal reaches the free-shipping threshold. Public
     * so CheckoutPreviewService computes the exact same number a real checkout
     * would, instead of re-implementing (and risking drifting from) this rule.
     */
    public BigDecimal calculateShippingFee(BigDecimal subtotal) {
        BigDecimal threshold = shippingProperties.freeShippingThreshold();
        if (threshold != null && subtotal.compareTo(threshold) >= 0) {
            return BigDecimal.ZERO;
        }
        return shippingProperties.flatFee() != null ? shippingProperties.flatFee() : BigDecimal.ZERO;
    }

    private String generateOrderCode() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String randomPart = String.format("%05d", ThreadLocalRandom.current().nextInt(100000));
        return "ORD" + datePart + randomPart;
    }

    private Map<String, Object> buildAddressSnapshot(Address address) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("receiverName", address.getReceiverName());
        snapshot.put("phone", address.getPhone());
        snapshot.put("province", address.getProvince());
        snapshot.put("district", address.getDistrict());
        snapshot.put("ward", address.getWard());
        snapshot.put("addressLine", address.getAddressLine());
        return snapshot;
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

    private OrderResponse assembleResponse(Order order) {
        List<OrderItemResponse> items = orderItemRepository.findAllByOrderIdOrderByIdAsc(order.getId()).stream()
                .map(orderMapper::toItemResponse)
                .toList();
        return toResponse(order, items);
    }

    private List<OrderResponse> assembleResponses(List<Order> orders) {
        if (orders.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<OrderItemResponse>> itemsByOrder = itemsByOrderId(orders);
        return orders.stream()
                .map(o -> toResponse(o, itemsByOrder.getOrDefault(o.getId(), List.of())))
                .toList();
    }

    private AdminOrderResponse assembleAdminResponse(Order order) {
        List<OrderItemResponse> items = orderItemRepository.findAllByOrderIdOrderByIdAsc(order.getId()).stream()
                .map(orderMapper::toItemResponse)
                .toList();
        return toAdminResponse(order, items);
    }

    private List<AdminOrderResponse> assembleAdminResponses(List<Order> orders) {
        if (orders.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<OrderItemResponse>> itemsByOrder = itemsByOrderId(orders);
        return orders.stream()
                .map(o -> toAdminResponse(o, itemsByOrder.getOrDefault(o.getId(), List.of())))
                .toList();
    }

    private Map<UUID, List<OrderItemResponse>> itemsByOrderId(List<Order> orders) {
        List<UUID> orderIds = orders.stream().map(Order::getId).toList();
        return orderItemRepository.findAllByOrderIdIn(orderIds).stream()
                .collect(Collectors.groupingBy(
                        i -> i.getOrder().getId(),
                        Collectors.mapping(orderMapper::toItemResponse, Collectors.toList())));
    }

    private OrderResponse toResponse(Order order, List<OrderItemResponse> items) {
        return new OrderResponse(
                order.getId(),
                order.getOrderCode(),
                order.getOrderStatus(),
                order.getPaymentStatus(),
                order.getPaymentMethod(),
                order.getSubtotalAmount(),
                order.getShippingFee(),
                order.getDiscountAmount(),
                order.getTotalAmount(),
                order.getNote(),
                items,
                order.getCreatedAt());
    }

    private AdminOrderResponse toAdminResponse(Order order, List<OrderItemResponse> items) {
        User user = order.getUser();
        return new AdminOrderResponse(
                order.getId(),
                order.getOrderCode(),
                user.getId(),
                user.getFullName(),
                user.getPhone(),
                order.getOrderStatus(),
                order.getPaymentStatus(),
                order.getPaymentMethod(),
                order.getSubtotalAmount(),
                order.getShippingFee(),
                order.getDiscountAmount(),
                order.getTotalAmount(),
                order.getNote(),
                order.getInternalNote(),
                items,
                order.getCreatedAt());
    }
}
