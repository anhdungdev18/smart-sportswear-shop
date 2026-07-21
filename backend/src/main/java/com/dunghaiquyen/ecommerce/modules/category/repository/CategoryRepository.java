package com.dunghaiquyen.ecommerce.modules.category.repository;

import com.dunghaiquyen.ecommerce.modules.category.entity.Category;
import com.dunghaiquyen.ecommerce.modules.category.entity.CategoryStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlug(String slug);

    Optional<Category> findBySlugAndStatus(String slug, CategoryStatus status);

    Optional<Category> findByIdAndStatus(UUID id, CategoryStatus status);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    List<Category> findAllByStatusOrderBySortOrderAscNameAsc(CategoryStatus status);

    boolean existsByParentId(UUID parentId);

    @Query("""
            select c
            from Category c
            left join fetch c.parent
            where c.status = :status
            order by c.sortOrder asc, c.name asc
            """)
    List<Category> findAllActiveWithParent(CategoryStatus status);
}
