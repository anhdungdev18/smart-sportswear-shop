package com.dunghaiquyen.ecommerce.modules.returns.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.common.exception.ResourceNotFoundException;
import com.dunghaiquyen.ecommerce.common.response.PageMeta;
import com.dunghaiquyen.ecommerce.modules.audit.service.AuditLogService;
import com.dunghaiquyen.ecommerce.modules.order.entity.Order;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderItem;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import com.dunghaiquyen.ecommerce.modules.order.repository.OrderItemRepository;
import com.dunghaiquyen.ecommerce.modules.order.repository.OrderRepository;
import com.dunghaiquyen.ecommerce.modules.payment.entity.Payment;
import com.dunghaiquyen.ecommerce.modules.payment.entity.PaymentProvider;
import com.dunghaiquyen.ecommerce.modules.payment.repository.PaymentRepository;
import com.dunghaiquyen.ecommerce.modules.returns.dto.AdminReturnListQuery;
import com.dunghaiquyen.ecommerce.modules.returns.dto.CreateRefundRequest;
import com.dunghaiquyen.ecommerce.modules.returns.dto.CreateReturnRequest;
import com.dunghaiquyen.ecommerce.modules.returns.dto.RefundResponse;
import com.dunghaiquyen.ecommerce.modules.returns.dto.ReturnItemRequest;
import com.dunghaiquyen.ecommerce.modules.returns.dto.ReturnItemResolutionRequest;
import com.dunghaiquyen.ecommerce.modules.returns.dto.ReturnItemResponse;
import com.dunghaiquyen.ecommerce.modules.returns.dto.ReturnResponse;
import com.dunghaiquyen.ecommerce.modules.returns.dto.UpdateRefundStatusRequest;
import com.dunghaiquyen.ecommerce.modules.returns.dto.UpdateReturnStatusRequest;
import com.dunghaiquyen.ecommerce.modules.returns.entity.Refund;
import com.dunghaiquyen.ecommerce.modules.returns.entity.RefundProvider;
import com.dunghaiquyen.ecommerce.modules.returns.entity.RefundStatus;
import com.dunghaiquyen.ecommerce.modules.returns.entity.Return;
import com.dunghaiquyen.ecommerce.modules.returns.entity.ReturnItem;
import com.dunghaiquyen.ecommerce.modules.returns.entity.ReturnItemResolution;
import com.dunghaiquyen.ecommerce.modules.returns.entity.ReturnStatus;
import com.dunghaiquyen.ecommerce.modules.returns.repository.RefundRepository;
import com.dunghaiquyen.ecommerce.modules.returns.repository.ReturnItemRepository;
import com.dunghaiquyen.ecommerce.modules.returns.repository.ReturnRepository;
import com.dunghaiquyen.ecommerce.modules.returns.repository.spec.ReturnSpecifications;
import com.dunghaiquyen.ecommerce.modules.user.entity.User;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owns both the Return lifecycle and the Refund actions a Return drives.
 * Refund money actions and Return status are kept in sync from exactly one
 * direction: completing a Refund moves its linked
 * Return to REFUNDED (see {@link #updateRefundStatus}) - a Return's own
 * status-update endpoint can never set REFUNDED directly, so there is only
 * one code path that can ever reach it.
 *
 * <p><b>Cardinality:</b> an order may accumulate multiple Return rows over
 * its lifetime, but at most one in a non-terminal status
 * (REQUESTED/APPROVED/RECEIVED) at a time - enforced here, not by a DB
 * constraint (same tradeoff already accepted for Shipment's "one per order"
 * rule).
 *
 * <p><b>Return window:</b> only enforced when the order's deliveredAt is
 * known (set starting this phase - see Order.deliveredAt's javadoc); orders
 * delivered before this column existed have a null deliveredAt and are
 * leniently allowed through rather than rejected on missing data.
 */
@Service
public class ReturnService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;
    private static final int RETURN_WINDOW_DAYS = 7;

    /** Customer self-cancel reuses this same map (only CANCELLED from REQUESTED is reachable that way) - same shared-transition-rules discipline OrderService.applyStatusTransition already establishes. */
    private static final Map<ReturnStatus, Set<ReturnStatus>> ALLOWED_TRANSITIONS = Map.of(
            ReturnStatus.REQUESTED, Set.of(ReturnStatus.APPROVED, ReturnStatus.REJECTED, ReturnStatus.CANCELLED),
            ReturnStatus.APPROVED, Set.of(ReturnStatus.RECEIVED, ReturnStatus.REJECTED, ReturnStatus.CANCELLED),
            ReturnStatus.RECEIVED, Set.of(),
            ReturnStatus.REJECTED, Set.of(),
            ReturnStatus.REFUNDED, Set.of(),
            ReturnStatus.CANCELLED, Set.of());

    private static final Set<ReturnStatus> ACTIVE_STATUSES =
            Set.of(ReturnStatus.REQUESTED, ReturnStatus.APPROVED, ReturnStatus.RECEIVED);

    private static final Map<RefundStatus, Set<RefundStatus>> REFUND_ALLOWED_TRANSITIONS = Map.of(
            RefundStatus.PENDING, Set.of(RefundStatus.COMPLETED, RefundStatus.FAILED, RefundStatus.CANCELLED),
            RefundStatus.PROCESSING, Set.of(),
            RefundStatus.COMPLETED, Set.of(),
            RefundStatus.FAILED, Set.of(),
            RefundStatus.CANCELLED, Set.of());

    private final ReturnRepository returnRepository;
    private final ReturnItemRepository returnItemRepository;
    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final AuditLogService auditLogService;

    public ReturnService(
            ReturnRepository returnRepository,
            ReturnItemRepository returnItemRepository,
            RefundRepository refundRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            PaymentRepository paymentRepository,
            AuditLogService auditLogService) {
        this.returnRepository = returnRepository;
        this.returnItemRepository = returnItemRepository;
        this.refundRepository = refundRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.auditLogService = auditLogService;
    }

    public record ListResult<T>(List<T> items, PageMeta meta) {
    }

    @Transactional
    public ReturnResponse createReturn(UUID userId, CreateReturnRequest request) {
        Order order = orderRepository.findById(request.orderId())
                .filter(o -> o.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new BusinessRuleException(
                    HttpStatus.UNPROCESSABLE_ENTITY, "Order must be delivered before requesting a return");
        }
        if (order.getDeliveredAt() != null
                && Instant.now().isAfter(order.getDeliveredAt().plus(RETURN_WINDOW_DAYS, ChronoUnit.DAYS))) {
            throw new BusinessRuleException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Return window has expired (" + RETURN_WINDOW_DAYS + " days from delivery)");
        }
        if (returnRepository.existsByOrderIdAndStatusIn(order.getId(), ACTIVE_STATUSES)) {
            throw new BusinessRuleException(
                    HttpStatus.CONFLICT, "An active return already exists for this order");
        }

        User user = order.getUser();
        Return returnRequest = new Return();
        returnRequest.setOrder(order);
        returnRequest.setUser(user);
        returnRequest.setReturnCode(generateCode("RET"));
        returnRequest.setStatus(ReturnStatus.REQUESTED);
        returnRequest.setReason(request.reason());
        returnRequest.setDescription(request.description());
        returnRequest.setRequestedAt(Instant.now());

        returnRequest = saveWithCodeRetry(returnRequest);

        for (ReturnItemRequest itemRequest : request.items()) {
            OrderItem orderItem = orderItemRepository.findById(itemRequest.orderItemId())
                    .filter(oi -> oi.getOrder().getId().equals(order.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Order item not found in this order"));
            if (itemRequest.quantity() > orderItem.getQuantity()) {
                throw new BusinessRuleException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Return quantity exceeds ordered quantity for " + orderItem.getSkuSnapshot());
            }
            ReturnItem returnItem = new ReturnItem();
            returnItem.setReturnRequest(returnRequest);
            returnItem.setOrderItem(orderItem);
            returnItem.setQuantity(itemRequest.quantity());
            returnItem.setReason(itemRequest.reason());
            returnItemRepository.save(returnItem);
        }

        return assembleResponse(returnRequest);
    }

    @Transactional(readOnly = true)
    public List<ReturnResponse> listMine(UUID userId) {
        return returnRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::assembleResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReturnResponse getDetail(UUID returnId, UUID callerId) {
        Return returnRequest = returnRepository.findById(returnId)
                .filter(r -> r.getUser().getId().equals(callerId))
                .orElseThrow(() -> new ResourceNotFoundException("Return not found"));
        return assembleResponse(returnRequest);
    }

    @Transactional
    public ReturnResponse cancelOwnReturn(UUID returnId, UUID userId) {
        Return returnRequest = returnRepository.findByIdForUpdate(returnId)
                .filter(r -> r.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Return not found"));
        applyTransition(returnRequest, ReturnStatus.CANCELLED, returnRequest.getUser());
        returnRequest.setResolvedAt(Instant.now());
        returnRequest = returnRepository.save(returnRequest);
        return assembleResponse(returnRequest);
    }

    @Transactional(readOnly = true)
    public ListResult<ReturnResponse> listForAdmin(AdminReturnListQuery query) {
        Specification<Return> spec = Specification.unrestricted();
        if (query.status() != null) {
            spec = spec.and(ReturnSpecifications.hasStatus(query.status()));
        }
        if (query.userId() != null) {
            spec = spec.and(ReturnSpecifications.belongsToUser(query.userId()));
        }
        if (query.orderId() != null) {
            spec = spec.and(ReturnSpecifications.belongsToOrder(query.orderId()));
        }
        Pageable pageable = PageRequest.of(
                resolvePageIndex(query.page()), resolveLimit(query.limit()), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Return> page = returnRepository.findAll(spec, pageable);
        List<ReturnResponse> items = assembleResponses(page.getContent());
        return new ListResult<>(items, PageMeta.from(page));
    }

    @Transactional(readOnly = true)
    public ReturnResponse getDetailForAdmin(UUID returnId) {
        Return returnRequest = returnRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found"));
        return assembleResponse(returnRequest);
    }

    /**
     * Admin-only transition. RECEIVED is the one target that also requires
     * item resolutions in the SAME call: every ReturnItem belonging to this
     * return must be covered (no partial resolution this phase) so the
     * return never sits in RECEIVED with some items still unresolved.
     * REFUNDED is deliberately not a reachable target here - see class
     * javadoc.
     */
    @Transactional
    public ReturnResponse updateStatus(UUID returnId, UpdateReturnStatusRequest request, User actor) {
        Return returnRequest = returnRepository.findByIdForUpdate(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found"));
        ReturnStatus previousStatus = returnRequest.getStatus();

        applyTransition(returnRequest, request.status(), actor);

        if (request.status() == ReturnStatus.APPROVED) {
            returnRequest.setApprovedAt(Instant.now());
        } else if (request.status() == ReturnStatus.RECEIVED) {
            resolveItems(returnRequest, request.items());
            returnRequest.setReceivedAt(Instant.now());
        } else if (request.status() == ReturnStatus.REJECTED || request.status() == ReturnStatus.CANCELLED) {
            returnRequest.setResolvedAt(Instant.now());
        }

        returnRequest = returnRepository.save(returnRequest);
        auditLogService.record(
                actor, "RETURN_STATUS_UPDATE", "Return", returnId.toString(),
                Map.of("status", previousStatus.name()), Map.of("status", returnRequest.getStatus().name()));
        return assembleResponse(returnRequest);
    }

    private void resolveItems(Return returnRequest, List<ReturnItemResolutionRequest> resolutions) {
        List<ReturnItem> items = returnItemRepository.findAllByReturnRequestIdOrderByIdAsc(returnRequest.getId());
        if (resolutions == null || resolutions.size() != items.size()) {
            throw new BusinessRuleException(
                    HttpStatus.UNPROCESSABLE_ENTITY, "Every return item must be resolved when marking RECEIVED");
        }
        Map<UUID, ReturnItemResolutionRequest> byItemId = resolutions.stream()
                .collect(java.util.stream.Collectors.toMap(ReturnItemResolutionRequest::returnItemId, r -> r));

        for (ReturnItem item : items) {
            ReturnItemResolutionRequest resolution = byItemId.get(item.getId());
            if (resolution == null) {
                throw new BusinessRuleException(
                        HttpStatus.UNPROCESSABLE_ENTITY, "Missing resolution for return item " + item.getId());
            }
            item.setConditionStatus(resolution.conditionStatus());
            item.setResolution(resolution.resolution());
            if (resolution.resolution() == ReturnItemResolution.REFUND) {
                BigDecimal maxRefund = item.getOrderItem().getUnitPriceSnapshot()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                if (resolution.refundAmount() == null || resolution.refundAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessRuleException(
                            HttpStatus.UNPROCESSABLE_ENTITY, "refundAmount is required when resolution is REFUND");
                }
                if (resolution.refundAmount().compareTo(maxRefund) > 0) {
                    throw new BusinessRuleException(
                            HttpStatus.UNPROCESSABLE_ENTITY,
                            "refundAmount exceeds the line value for return item " + item.getId());
                }
                item.setRefundAmount(resolution.refundAmount());
            } else {
                // Only a REFUND resolution may ever feed a Refund's amount - clearing this
                // on every other resolution means createRefund's sum can never accidentally
                // include a stray value left over from an admin's earlier mind-change.
                item.setRefundAmount(null);
            }
            returnItemRepository.save(item);
        }
    }

    /**
     * Computes the refund amount itself, from the items' own resolution -
     * never admin-entered (see CreateRefundRequest's javadoc). Does NOT move
     * the Return to REFUNDED yet - that only happens when this Refund
     * actually completes (see updateRefundStatus). Provider is read off the
     * order's most recent Payment row if one exists, else MANUAL (e.g. a
     * COD order that never created a payment session).
     */
    @Transactional
    public RefundResponse createRefund(UUID returnId, CreateRefundRequest request, User actor) {
        Return returnRequest = returnRepository.findByIdForUpdate(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found"));
        if (returnRequest.getStatus() != ReturnStatus.RECEIVED) {
            throw new BusinessRuleException(
                    HttpStatus.CONFLICT, "Return must be RECEIVED before a refund can be created");
        }

        List<ReturnItem> items = returnItemRepository.findAllByReturnRequestIdOrderByIdAsc(returnId);
        BigDecimal total = items.stream()
                .filter(i -> i.getResolution() == ReturnItemResolution.REFUND)
                .map(ReturnItem::getRefundAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException(
                    HttpStatus.UNPROCESSABLE_ENTITY, "No items were resolved as REFUND - nothing to refund");
        }

        Order order = returnRequest.getOrder();
        Payment latestPayment = paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(order.getId()).orElse(null);
        RefundProvider provider = latestPayment == null
                ? RefundProvider.MANUAL
                : (latestPayment.getProvider() == PaymentProvider.VNPAY ? RefundProvider.VNPAY : RefundProvider.COD);

        Refund refund = new Refund();
        refund.setReturnRequest(returnRequest);
        refund.setOrder(order);
        refund.setPayment(latestPayment);
        refund.setRefundCode(generateCode("RFD"));
        refund.setAmount(total);
        refund.setProvider(provider);
        refund.setStatus(RefundStatus.PENDING);
        refund.setReason(request.reason());
        refund.setCreatedBy(actor);

        refund = saveRefundWithCodeRetry(refund);
        auditLogService.record(
                actor, "REFUND_CREATE", "Refund", refund.getId().toString(),
                null, Map.of("amount", total.toString(), "provider", provider.name(), "status", refund.getStatus().name()));
        return toRefundResponse(refund);
    }

    @Transactional(readOnly = true)
    public List<RefundResponse> listRefundsForReturn(UUID returnId) {
        return refundRepository.findAllByReturnRequestIdOrderByCreatedAtDesc(returnId).stream()
                .map(this::toRefundResponse)
                .toList();
    }

    /**
     * Only this method can ever move a Return to REFUNDED - see class
     * javadoc. FAILED/CANCELLED leave the Return at RECEIVED so admin can
     * create another Refund attempt for it (same "multiple attempts against
     * one parent" shape Payment already allows for Orders).
     */
    @Transactional
    public RefundResponse updateRefundStatus(UUID refundId, UpdateRefundStatusRequest request, User actor) {
        Refund refund = refundRepository.findByIdForUpdate(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found"));
        RefundStatus current = refund.getStatus();
        if (!REFUND_ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(request.status())) {
            throw new BusinessRuleException(
                    HttpStatus.CONFLICT, "Cannot transition refund from " + current + " to " + request.status());
        }
        refund.setStatus(request.status());
        if (request.status() == RefundStatus.COMPLETED) {
            refund.setRefundedAt(Instant.now());
        }
        refund = refundRepository.save(refund);
        auditLogService.record(
                actor, "REFUND_STATUS_UPDATE", "Refund", refundId.toString(),
                Map.of("status", current.name()), Map.of("status", refund.getStatus().name()));

        if (request.status() == RefundStatus.COMPLETED && refund.getReturnRequest() != null) {
            Return returnRequest = returnRepository.findByIdForUpdate(refund.getReturnRequest().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Return not found"));
            returnRequest.setStatus(ReturnStatus.REFUNDED);
            returnRequest.setResolvedAt(Instant.now());
            returnRepository.save(returnRequest);
        }

        return toRefundResponse(refund);
    }

    private void applyTransition(Return returnRequest, ReturnStatus target, User actor) {
        ReturnStatus current = returnRequest.getStatus();
        if (!ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(target)) {
            throw new BusinessRuleException(
                    HttpStatus.CONFLICT, "Cannot transition return from " + current + " to " + target);
        }
        returnRequest.setStatus(target);
        returnRequest.setHandledBy(actor);
    }

    private Return saveWithCodeRetry(Return returnRequest) {
        try {
            return returnRepository.saveAndFlush(returnRequest);
        } catch (DataIntegrityViolationException ex) {
            returnRequest.setReturnCode(generateCode("RET"));
            return returnRepository.saveAndFlush(returnRequest);
        }
    }

    private Refund saveRefundWithCodeRetry(Refund refund) {
        try {
            return refundRepository.saveAndFlush(refund);
        } catch (DataIntegrityViolationException ex) {
            refund.setRefundCode(generateCode("RFD"));
            return refundRepository.saveAndFlush(refund);
        }
    }

    private String generateCode(String prefix) {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String randomPart = String.format("%05d", ThreadLocalRandom.current().nextInt(100000));
        return prefix + datePart + randomPart;
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

    private ReturnResponse assembleResponse(Return returnRequest) {
        List<ReturnItemResponse> items = returnItemRepository.findAllByReturnRequestIdOrderByIdAsc(returnRequest.getId())
                .stream()
                .map(this::toItemResponse)
                .toList();
        Order order = returnRequest.getOrder();
        return new ReturnResponse(
                returnRequest.getId(),
                order.getId(),
                order.getOrderCode(),
                returnRequest.getUser().getId(),
                returnRequest.getReturnCode(),
                returnRequest.getStatus(),
                returnRequest.getReason(),
                returnRequest.getDescription(),
                returnRequest.getRequestedAt(),
                returnRequest.getApprovedAt(),
                returnRequest.getReceivedAt(),
                returnRequest.getResolvedAt(),
                items,
                returnRequest.getCreatedAt(),
                returnRequest.getUpdatedAt());
    }

    /** Batch-loads all return lines for a page, avoiding one item query per return. */
    private List<ReturnResponse> assembleResponses(List<Return> returns) {
        if (returns.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<ReturnItemResponse>> itemsByReturn = returnItemRepository
                .findAllByReturnRequestIdInOrderByIdAsc(returns.stream().map(Return::getId).toList())
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        item -> item.getReturnRequest().getId(),
                        java.util.stream.Collectors.mapping(this::toItemResponse, java.util.stream.Collectors.toList())));
        return returns.stream().map(returnRequest -> {
            Order order = returnRequest.getOrder();
            return new ReturnResponse(
                    returnRequest.getId(), order.getId(), order.getOrderCode(), returnRequest.getUser().getId(),
                    returnRequest.getReturnCode(), returnRequest.getStatus(), returnRequest.getReason(),
                    returnRequest.getDescription(), returnRequest.getRequestedAt(), returnRequest.getApprovedAt(),
                    returnRequest.getReceivedAt(), returnRequest.getResolvedAt(),
                    itemsByReturn.getOrDefault(returnRequest.getId(), List.of()),
                    returnRequest.getCreatedAt(), returnRequest.getUpdatedAt());
        }).toList();
    }

    private ReturnItemResponse toItemResponse(ReturnItem item) {
        OrderItem orderItem = item.getOrderItem();
        return new ReturnItemResponse(
                item.getId(),
                orderItem.getId(),
                orderItem.getProductNameSnapshot(),
                orderItem.getSkuSnapshot(),
                item.getQuantity(),
                item.getReason(),
                item.getConditionStatus(),
                item.getResolution(),
                item.getRefundAmount());
    }

    private RefundResponse toRefundResponse(Refund refund) {
        return new RefundResponse(
                refund.getId(),
                refund.getReturnRequest() != null ? refund.getReturnRequest().getId() : null,
                refund.getOrder().getId(),
                refund.getPayment() != null ? refund.getPayment().getId() : null,
                refund.getRefundCode(),
                refund.getAmount(),
                refund.getProvider(),
                refund.getStatus(),
                refund.getReason(),
                refund.getRefundedAt(),
                refund.getCreatedAt(),
                refund.getUpdatedAt());
    }
}
