package com.dunghaiquyen.ecommerce.modules.returns.repository.spec;

import com.dunghaiquyen.ecommerce.modules.returns.entity.Return;
import com.dunghaiquyen.ecommerce.modules.returns.entity.ReturnStatus;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class ReturnSpecifications {

    private ReturnSpecifications() {
    }

    public static Specification<Return> hasStatus(ReturnStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Return> belongsToUser(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Return> belongsToOrder(UUID orderId) {
        return (root, query, cb) -> cb.equal(root.get("order").get("id"), orderId);
    }
}
