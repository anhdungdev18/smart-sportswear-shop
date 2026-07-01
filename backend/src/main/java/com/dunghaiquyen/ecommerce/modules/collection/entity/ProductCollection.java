package com.dunghaiquyen.ecommerce.modules.collection.entity;

import com.dunghaiquyen.ecommerce.common.entity.AbstractCreatedAtEntity;
import com.dunghaiquyen.ecommerce.modules.product.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Join record for the Product ↔ Collection many-to-many relationship.
 * Uniqueness of (product_id, collection_id) is enforced by the DB constraint
 * uq_product_collections (V10); duplicate-insert attempts surface as
 * DataIntegrityViolationException and are mapped to a 409 in
 * CollectionService.linkProduct.
 *
 * <p>No updated_at (uses AbstractCreatedAtEntity): a link is created once
 * and either kept or deleted, never partially updated in place.
 */
@Getter
@Setter
@Entity
@Table(name = "product_collections")
public class ProductCollection extends AbstractCreatedAtEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;
}
