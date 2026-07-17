package com.dunghaiquyen.ecommerce.modules.combo.repository;

import com.dunghaiquyen.ecommerce.modules.combo.entity.Combo;
import com.dunghaiquyen.ecommerce.modules.combo.entity.ComboStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComboRepository extends JpaRepository<Combo, UUID> {

    /**
     * Active combos with their product rows loaded, for the checkout discount pass.
     * Only product ids are read there, so the product itself stays lazy.
     */
    @Query("select distinct c from Combo c left join fetch c.products where c.status = :status")
    List<Combo> findAllByStatusWithProducts(@Param("status") ComboStatus status);

    /** All combos with products AND their product entities, for the admin list (needs product names). */
    @Query("select distinct c from Combo c left join fetch c.products cp left join fetch cp.product")
    List<Combo> findAllWithProducts();

    /** Single combo with products and their product entities, for admin detail/edit. */
    @Query("select c from Combo c left join fetch c.products cp left join fetch cp.product where c.id = :id")
    Optional<Combo> findByIdWithProducts(@Param("id") UUID id);
}
