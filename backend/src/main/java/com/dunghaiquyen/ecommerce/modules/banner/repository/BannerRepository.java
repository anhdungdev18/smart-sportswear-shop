package com.dunghaiquyen.ecommerce.modules.banner.repository;

import com.dunghaiquyen.ecommerce.modules.banner.entity.Banner;
import com.dunghaiquyen.ecommerce.modules.banner.entity.BannerPlacement;
import com.dunghaiquyen.ecommerce.modules.banner.entity.BannerStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BannerRepository extends JpaRepository<Banner, UUID>, JpaSpecificationExecutor<Banner> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, UUID id);

    /** Public active banner = ACTIVE status and currently within its own time window. */
    @Query("select b from Banner b where b.status = :status and b.placement = :placement "
            + "and (b.startsAt is null or b.startsAt <= :now) and (b.endsAt is null or b.endsAt >= :now) "
            + "order by b.createdAt desc")
    List<Banner> findAllActiveByPlacement(
            @Param("status") BannerStatus status, @Param("placement") BannerPlacement placement, @Param("now") Instant now);
}
