package com.dunghaiquyen.ecommerce.modules.page.repository;

import com.dunghaiquyen.ecommerce.modules.page.entity.Page;
import com.dunghaiquyen.ecommerce.modules.page.entity.PageStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PageRepository extends JpaRepository<Page, UUID>, JpaSpecificationExecutor<Page> {

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    Optional<Page> findBySlugAndStatus(String slug, PageStatus status);
}
