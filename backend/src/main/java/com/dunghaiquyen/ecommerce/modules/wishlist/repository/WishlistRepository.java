package com.dunghaiquyen.ecommerce.modules.wishlist.repository;

import com.dunghaiquyen.ecommerce.modules.wishlist.entity.Wishlist;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {

    Optional<Wishlist> findByUserId(UUID userId);
}
