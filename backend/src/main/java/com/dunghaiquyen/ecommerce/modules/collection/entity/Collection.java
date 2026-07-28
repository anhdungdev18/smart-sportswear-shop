package com.dunghaiquyen.ecommerce.modules.collection.entity;

import com.dunghaiquyen.ecommerce.common.entity.AbstractAuditEntity;
import com.dunghaiquyen.ecommerce.modules.brand.entity.Brand;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * Marketing/merchandising grouping that can span multiple product types, categories
 * and brands - see SPORTSWEAR_CATALOG_MODEL_V2.md section 3.3.
 *
 * <p>The time-window fields (startsAt/endsAt) let admin pre-schedule a
 * collection so it goes live automatically at a given moment. The public
 * listing uses both status == ACTIVE AND the time window before exposing a
 * collection, so even an ACTIVE status collection is hidden before its
 * startsAt or after its endsAt.
 */
@Getter
@Setter
@Entity
@Table(name = "collections")
public class Collection extends AbstractAuditEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_type", nullable = false, length = 30)
    private CollectionType collectionType;

    /** Optional brand tag - see V20 migration. Null = not tied to any brand. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(length = 50)
    private String season;

    private Integer year;

    @Column(name = "banner_image_url", length = 500)
    private String bannerImageUrl;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CollectionStatus status = CollectionStatus.DRAFT;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "is_featured", nullable = false)
    private boolean featured = false;
}
