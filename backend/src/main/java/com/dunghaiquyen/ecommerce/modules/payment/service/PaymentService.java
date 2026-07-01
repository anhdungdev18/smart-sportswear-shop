package com.dunghaiquyen.ecommerce.modules.payment.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.common.exception.ResourceNotFoundException;
import com.dunghaiquyen.ecommerce.config.AppVnpayProperties;
import com.dunghaiquyen.ecommerce.modules.order.entity.Order;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import com.dunghaiquyen.ecommerce.modules.order.entity.PaymentMethod;
import com.dunghaiquyen.ecommerce.modules.order.repository.OrderRepository;
import com.dunghaiquyen.ecommerce.modules.payment.dto.CreatePaymentRequest;
import com.dunghaiquyen.ecommerce.modules.payment.dto.CreatePaymentResponse;
import com.dunghaiquyen.ecommerce.modules.payment.dto.PaymentResponse;
import com.dunghaiquyen.ecommerce.modules.payment.entity.Payment;
import com.dunghaiquyen.ecommerce.modules.payment.entity.PaymentProvider;
import com.dunghaiquyen.ecommerce.modules.payment.entity.PaymentStatus;
import com.dunghaiquyen.ecommerce.modules.payment.repository.PaymentRepository;
import com.dunghaiquyen.ecommerce.modules.user.entity.UserRole;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * VNPay sandbox-shaped flow only - see VnpaySignatureService's javadoc for what
 * is and is not faithfully replicated from the real VNPay protocol. Nothing here
 * touches stock/InventoryService or creates Order rows: by construction the
 * callback path can only ever move an existing Payment/Order between payment
 * states, never re-run order creation or stock reservation (Phase G's job).
 */
@Service
public class PaymentService {

    private static final String VNP_SUCCESS_CODE = "00";
    private static final String VNP_USER_CANCELLED_CODE = "24";

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final VnpaySignatureService signatureService;
    private final AppVnpayProperties vnpayProperties;

    public PaymentService(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            VnpaySignatureService signatureService,
            AppVnpayProperties vnpayProperties) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.signatureService = signatureService;
        this.vnpayProperties = vnpayProperties;
    }

    /**
     * Order eligibility + the "call this twice for the same order" rule:
     * - order not found / not owned by caller -> 404.
     * - order.paymentMethod != VNPAY -> 422 (COD orders have no online session,
     *   per PHASE1_SPEC.md 6.7 - "order COD thì... không có online payment session").
     * - order.orderStatus == CANCELLED -> 422 (nothing to pay for any more).
     * - order.paymentStatus == PAID -> 422 (already settled, no second session).
     * - if the order's MOST RECENT payment attempt is still PENDING: re-issue
     *   that SAME session (same transactionRef/URL) instead of creating a new
     *   row. Chosen over "always insert a new row" because a customer calling
     *   create twice in a row (double click, page refresh) is almost certainly
     *   trying to resume the one in-flight attempt, not start a second
     *   competing one - having two simultaneously-PENDING sessions for the same
     *   order would make "which one does the next callback belong to" murky.
     *   A FAILED/CANCELLED latest attempt still allows a fresh row (ERD_PHASE1.md
     *   9.14: "một order có thể có nhiều bản ghi payment theo lịch sử retry").
     *
     * Concurrency: the order row is locked (findByIdForUpdate) before the
     * "is there a PENDING attempt already" read - without this, two concurrent
     * calls for the same order could both read "no PENDING attempt yet" and
     * both insert a competing PENDING row. The lock serializes the whole
     * read-then-decide-then-write sequence per order: the second caller blocks
     * until the first commits, then re-reads and finds the first one's PENDING
     * row, landing in the reuse branch instead of inserting a duplicate.
     */
    @Transactional
    public CreatePaymentResponse createPaymentSession(UUID userId, CreatePaymentRequest request) {
        Order order = orderRepository.findByIdForUpdate(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Order not found");
        }
        if (order.getPaymentMethod() != PaymentMethod.VNPAY) {
            throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Order is not eligible for online payment");
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Order is cancelled");
        }
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Order is already paid");
        }

        Optional<Payment> latest = paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(order.getId());
        if (latest.isPresent() && latest.get().getStatus() == PaymentStatus.PENDING) {
            Payment existing = latest.get();
            return new CreatePaymentResponse(buildPaymentUrl(existing), existing.getTransactionRef());
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setProvider(PaymentProvider.VNPAY);
        payment.setTransactionRef(generateTransactionRef());
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING);
        try {
            payment = paymentRepository.save(payment);
        } catch (DataIntegrityViolationException ex) {
            // transactionRef collision (vanishingly unlikely) - retry once with a
            // fresh ref rather than fail the whole checkout-payment flow.
            payment.setTransactionRef(generateTransactionRef());
            payment = paymentRepository.save(payment);
        }

        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            order.setPaymentStatus(PaymentStatus.PENDING);
            orderRepository.save(order);
        }

        return new CreatePaymentResponse(buildPaymentUrl(payment), payment.getTransactionRef());
    }

    /**
     * Idempotency contract (PHASE1_SPEC.md 6.7 + this phase's explicit ask):
     * - invalid/missing checksum -> reject, no row touched at all.
     * - unknown transactionRef -> 404, no row touched.
     * - payment already in a TERMINAL state (PAID/FAILED/CANCELLED/REFUNDED) ->
     *   no-op, return the same "processed" success response. This is the crux of
     *   "a payment success must not apply twice": the lock (findByTransactionRefForUpdate)
     *   plus this status check together mean two concurrent deliveries for the
     *   same transactionRef can never both observe PENDING and both write.
     * - payment still PENDING -> apply exactly once, mapping vnp_ResponseCode to
     *   the matching terminal PaymentStatus. OrderStatus itself is never
     *   touched here - a failed/cancelled payment does not auto-cancel the
     *   order, it just leaves it payable again (the customer can call
     *   create-payment-session again, landing in the "latest attempt is
     *   FAILED/CANCELLED -> new row" branch above).
     *
     * Order-already-CANCELLED rule (deliberately chosen over silently mirroring
     * onto Order.paymentStatus): if the order was cancelled while this payment
     * was in flight, Payment.status is still resolved and saved - that is what
     * the gateway actually reported, and dropping it would lose the one piece
     * of data a future refund/reconciliation flow would need - but
     * Order.paymentStatus is deliberately left UNTOUCHED. Phase H has no
     * refund flow, so there is no safe way to represent or unwind "PAID" on a
     * cancelled order through Order.paymentStatus alone; leaving it as it was
     * (whatever it was before this callback) avoids writing a state combination
     * the rest of the system has no defined handling for. This is a single
     * unconditional rule applied to every response code, not a special case
     * per outcome, so there is nothing implicit to keep in sync.
     */
    @Transactional
    public Map<String, Object> handleCallback(Map<String, String> params) {
        if (!signatureService.verify(params)) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, "Invalid checksum");
        }
        String transactionRef = params.get("vnp_TxnRef");
        if (transactionRef == null || transactionRef.isBlank()) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, "Missing transaction reference");
        }

        Payment payment = paymentRepository.findByTransactionRefForUpdate(transactionRef)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return Map.of();
        }

        String responseCode = params.get("vnp_ResponseCode");
        Order order = payment.getOrder();
        PaymentStatus resolvedStatus = resolveStatus(responseCode);
        payment.setStatus(resolvedStatus);
        if (resolvedStatus == PaymentStatus.PAID) {
            payment.setPaidAt(Instant.now());
        }
        payment.setRawPayloadJson(new LinkedHashMap<>(params));
        paymentRepository.save(payment);

        if (order.getOrderStatus() != OrderStatus.CANCELLED) {
            order.setPaymentStatus(resolvedStatus);
            orderRepository.save(order);
        }
        return Map.of();
    }

    private PaymentStatus resolveStatus(String responseCode) {
        if (VNP_SUCCESS_CODE.equals(responseCode)) {
            return PaymentStatus.PAID;
        }
        if (VNP_USER_CANCELLED_CODE.equals(responseCode)) {
            return PaymentStatus.CANCELLED;
        }
        return PaymentStatus.FAILED;
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrder(UUID orderId, UUID callerId, UserRole callerRole) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (callerRole == UserRole.CUSTOMER && !order.getUser().getId().equals(callerId)) {
            throw new ResourceNotFoundException("Order not found");
        }
        return paymentRepository.findAllByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .map(this::toResponse)
                .toList();
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getProvider(),
                payment.getTransactionRef(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaidAt(),
                payment.getCreatedAt());
    }

    private String generateTransactionRef() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        return "TXN" + datePart + randomPart;
    }

    private String buildPaymentUrl(Payment payment) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnpayProperties.tmnCode());
        params.put(
                "vnp_Amount",
                payment.getAmount().multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", payment.getTransactionRef());
        params.put("vnp_OrderInfo", "Payment for order " + payment.getOrder().getOrderCode());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnpayProperties.returnUrl());
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        String hash = signatureService.hash(params);
        String query = signatureService.buildQueryString(params);
        return vnpayProperties.payUrl() + "?" + query + "&vnp_SecureHash=" + hash;
    }
}
