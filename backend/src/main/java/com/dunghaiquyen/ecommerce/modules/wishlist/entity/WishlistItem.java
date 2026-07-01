package com.dunghaiquyen.ecommerce.modules.wishlist.entity;

import com.dunghaiquyen.ecommerce.common.entity.AbstractCreatedAtEntity;
import com.dunghaiquyen.ecommerce.modules.product.entity.Product;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * No updatedAt by design, same reasoning as OrderItem: a wishlist item is
 * either present or gone, never edited in place. V3's uq_wishlist_items
 * (wishlist_id, product_id) is what actually prevents duplicates at the DB
 * level - the application-layer check exists only to return a clean 409
 * instead of letting the race fall through to a raw constraint violation.
 */
@Getter
@Setter
@Entity
@Table(name = "wishlist_items")
public class WishlistItem extends AbstractCreatedAtEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wishlist_id", nullable = false)
    private Wishlist wishlist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
