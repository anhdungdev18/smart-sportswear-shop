package com.dunghaiquyen.ecommerce.modules.collection.repository;

import com.dunghaiquyen.ecommerce.modules.collection.entity.Collection;
import com.dunghaiquyen.ecommerce.modules.collection.entity.CollectionStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CollectionRepository extends JpaRepository<Collection, UUID>, JpaSpecificationExecutor<Collection> {

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    Optional<Collection> findBySlug(String slug);

    /**
     * Public listing: status=ACTIVE AND currently within its own time window.
     * Null startsAt/endsAt means unbounded. Ordered by sort_order ASC, then
     * createdAt DESC for stable tie-breaking.
     */
    @Query("select c from Collection c where c.status = :status "
            + "and (c.startsAt is null or c.startsAt <= :now) "
            + "and (c.endsAt is null or c.endsAt >= :now) "
            + "order by c.sortOrder asc, c.createdAt desc")
    List<Collection> findAllActive(@Param("status") CollectionStatus status, @Param("now") Instant now);
}
