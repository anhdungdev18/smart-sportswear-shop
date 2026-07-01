package com.dunghaiquyen.ecommerce.modules.cart.repository;

import com.dunghaiquyen.ecommerce.modules.cart.entity.Cart;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByUserId(UUID userId);

    Optional<Cart> findBySessionId(String sessionId);

    /**
     * Row-locked read used by OrderService at the start of checkout, so two
     * concurrent checkout requests for the SAME cart cannot both pass the
     * "cart has items" check before either has cleared it: the second request
     * blocks here until the first commits (or rolls back), then re-reads the
     * cart's items fresh - by which point the first request has already
     * deleted them if it succeeded.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cart c where c.user.id = :userId")
    Optional<Cart> findByUserIdForUpdate(@Param("userId") UUID userId);
}
