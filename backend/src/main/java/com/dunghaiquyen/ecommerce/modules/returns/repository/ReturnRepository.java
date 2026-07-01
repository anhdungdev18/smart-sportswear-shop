package com.dunghaiquyen.ecommerce.modules.returns.repository;

import com.dunghaiquyen.ecommerce.modules.returns.entity.Return;
import com.dunghaiquyen.ecommerce.modules.returns.entity.ReturnStatus;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReturnRepository extends JpaRepository<Return, UUID>, JpaSpecificationExecutor<Return> {

    boolean existsByOrderIdAndStatusIn(UUID orderId, Collection<ReturnStatus> statuses);

    List<Return> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    /** Row-locked read - serializes concurrent status updates/self-cancel on the same return, same pattern as OrderRepository.findByIdForUpdate. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Return r where r.id = :id")
    Optional<Return> findByIdForUpdate(@Param("id") UUID id);
}
