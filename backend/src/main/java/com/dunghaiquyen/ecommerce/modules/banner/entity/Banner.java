package com.dunghaiquyen.ecommerce.modules.banner.entity;

import com.dunghaiquyen.ecommerce.common.entity.AbstractAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/** A named slot (by placement) holding an ordered set of BannerItem slides - public listing only ever returns ACTIVE banners currently within their own time window. */
@Getter
@Setter
@Entity
@Table(name = "banners")
public class Banner extends AbstractAuditEntity {

    @Column(nullable = false, length = 180)
    private String name;

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BannerPlacement placement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BannerStatus status = BannerStatus.DRAFT;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;
}
