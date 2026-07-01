package com.dunghaiquyen.ecommerce.modules.order.repository.spec;

import com.dunghaiquyen.ecommerce.modules.order.entity.Order;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import com.dunghaiquyen.ecommerce.modules.order.entity.PaymentMethod;
import com.dunghaiquyen.ecommerce.modules.payment.entity.PaymentStatus;
import com.dunghaiquyen.ecommerce.modules.user.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class OrderSpecifications {

    private OrderSpecifications() {
    }

    public static Specification<Order> belongsToUser(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    /**
     * Side-effect specification (no real predicate) that fetch-joins the
     * ordering user so AdminOrderResponse's customerName/customerPhone fields
     * do not lazy-load once per row across an admin list page (N+1). Guarded
     * against Spring Data's separate COUNT query pass, same as
     * ProductSpecifications.fetchBrandAndCategory().
     */
    public static Specification<Order> fetchUser() {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType())) {
                root.fetch("user", JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, cb) -> cb.equal(root.get("orderStatus"), status);
    }

    public static Specification<Order> hasPaymentStatus(PaymentStatus paymentStatus) {
        return (root, query, cb) -> cb.equal(root.get("paymentStatus"), paymentStatus);
    }

    public static Specification<Order> hasPaymentMethod(PaymentMethod paymentMethod) {
        return (root, query, cb) -> cb.equal(root.get("paymentMethod"), paymentMethod);
    }

    public static Specification<Order> createdFrom(Instant from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<Order> createdTo(Instant to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    /**
     * API_SPEC_PHASE1.md 10.1 describes one "keyword" param meant to search by
     * order code, customer name, or phone all at once - implemented as an OR
     * across order_code and a left join to the ordering user.
     */
    public static Specification<Order> keywordMatches(String keyword) {
        return (root, query, cb) -> {
            Join<Order, User> userJoin = root.join("user", JoinType.LEFT);
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("orderCode")), pattern),
                    cb.like(cb.lower(userJoin.get("fullName")), pattern),
                    cb.like(userJoin.get("phone"), "%" + keyword + "%"));
        };
    }
}
