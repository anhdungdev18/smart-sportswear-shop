package com.dunghaiquyen.ecommerce.modules.recommendation.repository;

import com.dunghaiquyen.ecommerce.modules.recommendation.entity.AssociationRuleRebuildLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssociationRuleRebuildLogRepository extends JpaRepository<AssociationRuleRebuildLog, UUID> {
}