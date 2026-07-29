package com.dunghaiquyen.ecommerce.modules.returns.repository;

import com.dunghaiquyen.ecommerce.modules.returns.entity.ReturnItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReturnItemRepository extends JpaRepository<ReturnItem, UUID> {

    List<ReturnItem> findAllByReturnRequestIdOrderByIdAsc(UUID returnId);

    List<ReturnItem> findAllByReturnRequestIdInOrderByIdAsc(List<UUID> returnIds);
}
