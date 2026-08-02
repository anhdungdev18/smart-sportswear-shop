package com.dunghaiquyen.ecommerce.modules.payment.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.common.exception.ResourceNotFoundException;
import com.dunghaiquyen.ecommerce.common.time.AppTimeZone;
import com.dunghaiquyen.ecommerce.config.AppVnpayProperties;
import com.dunghaiquyen.ecommerce.modules.order.entity.Order;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import com.dunghaiquyen.ecommerce.modules.order.repository.OrderRepository;
import com.dunghaiquyen.ecommerce.modules.payment.entity.Payment;
import com.dunghaiquyen.ecommerce.modules.payment.entity.PaymentStatus;
import com.dunghaiquyen.ecommerce.modules.payment.repository.PaymentRepository;
import com.dunghaiquyen.ecommerce.modules.returns.entity.Refund;
import com.dunghaiquyen.ecommerce.modules.returns.entity.RefundStatus;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
public class VnpayTransactionService {
    private static final String VERSION = "2.1.0";
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final AppVnpayProperties properties;
    private final VnpaySignatureService signatures;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final RestClient restClient;

    public VnpayTransactionService(AppVnpayProperties properties, VnpaySignatureService signatures,
            PaymentRepository paymentRepository, OrderRepository orderRepository, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.signatures = signatures;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.restClient = restClientBuilder.build();
    }

    @Transactional
    public Map<String, Object> queryPayment(UUID paymentId, String ipAddress) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        requireTransactionDate(payment);
        String requestId = requestId();
        String createDate = now();
        String orderInfo = "Query payment " + payment.getOrder().getOrderCode();
        Map<String, String> body = base(requestId, "querydr");
        body.put("vnp_TxnRef", payment.getTransactionRef());
        if (payment.getGatewayTransactionNo() != null) body.put("vnp_TransactionNo", payment.getGatewayTransactionNo());
        body.put("vnp_TransactionDate", payment.getTransactionDate());
        body.put("vnp_CreateDate", createDate);
        body.put("vnp_IpAddr", safeIp(ipAddress));
        body.put("vnp_OrderInfo", orderInfo);
        body.put("vnp_SecureHash", signatures.hashRaw(pipe(requestId, VERSION, "querydr", properties.tmnCode(),
                payment.getTransactionRef(), payment.getTransactionDate(), createDate, safeIp(ipAddress), orderInfo)));
        Map<String, Object> response = post(body);
        verifyQueryResponse(response);
        if ("00".equals(string(response, "vnp_ResponseCode"))) {
            applyQueriedPaymentStatus(payment, string(response, "vnp_TransactionStatus"), response);
        }
        return response;
    }

    public Map<String, Object> refund(Refund refund, String createdBy, String ipAddress) {
        Payment payment = refund.getPayment();
        if (payment == null || payment.getStatus() != PaymentStatus.PAID) {
            throw new BusinessRuleException(HttpStatus.CONFLICT, "A paid VNPay transaction is required for refund");
        }
        requireTransactionDate(payment);
        String requestId = requestId();
        String createDate = now();
        String transactionType = refund.getAmount().compareTo(payment.getAmount()) >= 0 ? "02" : "03";
        String amount = refund.getAmount().movePointRight(2).toBigIntegerExact().toString();
        String orderInfo = "Refund " + refund.getRefundCode();
        String actor = sanitize(createdBy, "system");
        String transactionNo = sanitize(payment.getGatewayTransactionNo(), "");
        Map<String, String> body = base(requestId, "refund");
        body.put("vnp_TransactionType", transactionType);
        body.put("vnp_TxnRef", payment.getTransactionRef());
        body.put("vnp_Amount", amount);
        body.put("vnp_TransactionNo", transactionNo);
        body.put("vnp_TransactionDate", payment.getTransactionDate());
        body.put("vnp_CreateBy", actor);
        body.put("vnp_CreateDate", createDate);
        body.put("vnp_IpAddr", safeIp(ipAddress));
        body.put("vnp_OrderInfo", orderInfo);
        body.put("vnp_SecureHash", signatures.hashRaw(pipe(requestId, VERSION, "refund", properties.tmnCode(),
                transactionType, payment.getTransactionRef(), amount, transactionNo, payment.getTransactionDate(),
                actor, createDate, safeIp(ipAddress), orderInfo)));
        Map<String, Object> response = post(body);
        verifyRefundResponse(response);
        refund.setGatewayRequestId(requestId);
        refund.setGatewayResponseJson(response);
        refund.setGatewayTransactionNo(string(response, "vnp_TransactionNo"));
        String responseCode = string(response, "vnp_ResponseCode");
        String transactionStatus = string(response, "vnp_TransactionStatus");
        refund.setStatus("00".equals(responseCode) && "00".equals(transactionStatus)
                ? RefundStatus.COMPLETED
                : ("00".equals(responseCode) && List.of("01", "05", "06").contains(transactionStatus)
                        ? RefundStatus.PROCESSING : RefundStatus.FAILED));
        return response;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(Map<String, String> body) {
        try {
            Map<String, Object> response = restClient.post().uri(properties.transactionApiUrl())
                    .body(body).retrieve().body(Map.class);
            if (response == null) throw new IllegalStateException("Empty VNPay response");
            return new LinkedHashMap<>(response);
        } catch (Exception ex) {
            throw new BusinessRuleException(HttpStatus.BAD_GATEWAY, "VNPay transaction API is unavailable");
        }
    }

    private void applyQueriedPaymentStatus(Payment payment, String transactionStatus, Map<String, Object> response) {
        PaymentStatus status = "00".equals(transactionStatus) ? PaymentStatus.PAID
                : ("01".equals(transactionStatus) ? PaymentStatus.PENDING : PaymentStatus.FAILED);
        payment.setStatus(status);
        payment.setGatewayTransactionNo(string(response, "vnp_TransactionNo"));
        payment.setBankCode(string(response, "vnp_BankCode"));
        payment.setRawPayloadJson(response);
        paymentRepository.save(payment);
        Order order = orderRepository.findByIdForUpdate(payment.getOrder().getId()).orElseThrow();
        order.setPaymentStatus(status);
        if (order.getOrderStatus() == OrderStatus.CANCELLED && status == PaymentStatus.PAID) {
            order.setInternalNote("VNPay QueryDR found a successful payment after cancellation - refund required");
        }
        orderRepository.save(order);
    }

    private void verifyQueryResponse(Map<String, Object> response) {
        verify(response, pipeValues(response, "vnp_ResponseId", "vnp_Command", "vnp_ResponseCode", "vnp_Message",
                "vnp_TmnCode", "vnp_TxnRef", "vnp_Amount", "vnp_BankCode", "vnp_PayDate",
                "vnp_TransactionNo", "vnp_TransactionType", "vnp_TransactionStatus", "vnp_OrderInfo",
                "vnp_PromotionCode", "vnp_PromotionAmount"));
    }

    private void verifyRefundResponse(Map<String, Object> response) {
        verify(response, pipeValues(response, "vnp_ResponseId", "vnp_Command", "vnp_ResponseCode", "vnp_Message",
                "vnp_TmnCode", "vnp_TxnRef", "vnp_Amount", "vnp_BankCode", "vnp_PayDate",
                "vnp_TransactionNo", "vnp_TransactionType", "vnp_TransactionStatus", "vnp_OrderInfo"));
    }

    private void verify(Map<String, Object> response, String data) {
        String received = string(response, "vnp_SecureHash");
        if (!properties.tmnCode().equals(string(response, "vnp_TmnCode"))
                || !signatures.hashRaw(data).equalsIgnoreCase(received)) {
            throw new BusinessRuleException(HttpStatus.BAD_GATEWAY, "Invalid VNPay transaction response signature");
        }
    }

    private Map<String, String> base(String requestId, String command) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("vnp_RequestId", requestId); body.put("vnp_Version", VERSION);
        body.put("vnp_Command", command); body.put("vnp_TmnCode", properties.tmnCode());
        return body;
    }

    private void requireTransactionDate(Payment payment) {
        if (payment.getTransactionDate() == null) {
            throw new BusinessRuleException(HttpStatus.CONFLICT, "Payment has no VNPay transaction date");
        }
    }

    private String requestId() { return UUID.randomUUID().toString().replace("-", ""); }
    private String now() { return ZonedDateTime.now(AppTimeZone.ZONE).format(TIMESTAMP); }
    private String safeIp(String ip) { return sanitize(ip, "127.0.0.1"); }
    private String sanitize(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }
    private String string(Map<String, ?> map, String key) { Object value = map.get(key); return value == null ? "" : value.toString(); }
    private String pipe(String... values) { return String.join("|", values); }
    private String pipeValues(Map<String, Object> response, String... keys) {
        String[] values = new String[keys.length];
        for (int i = 0; i < keys.length; i++) values[i] = string(response, keys[i]);
        return pipe(values);
    }
}
