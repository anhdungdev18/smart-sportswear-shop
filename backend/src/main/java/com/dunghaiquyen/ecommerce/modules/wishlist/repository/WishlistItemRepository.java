package com.dunghaiquyen.ecommerce.modules.wishlist.repository;

import com.dunghaiquyen.ecommerce.modules.wishlist.entity.WishlistItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, UUID> {

    boolean existsByWishlistIdAndProductId(UUID wishlistId, UUID productId);

    Optional<WishlistItem> findByWishlistIdAndProductId(UUID wishlistId, UUID productId);

    /**
     * Fetch-joins only product (a ManyToOne, so no row duplication). Thumbnails
     * are resolved separately via ProductImageRepository.findAllByProductIdIn,
     * same as CartService - fetch-joining the images OneToMany here would
     * duplicate this row once per image.
     */
    @Query("select wi from WishlistItem wi join fetch wi.product p "
            + "where wi.wishlist.id = :wishlistId order by wi.createdAt asc")
    List<WishlistItem> findAllByWishlistIdWithProduct(@Param("wishlistId") UUID wishlistId);
}
