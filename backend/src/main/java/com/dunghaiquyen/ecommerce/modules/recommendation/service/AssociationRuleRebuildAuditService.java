package com.dunghaiquyen.ecommerce.modules.recommendation.service;

import com.dunghaiquyen.ecommerce.modules.recommendation.entity.AssociationRuleRebuildLog;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RebuildStatus;
import com.dunghaiquyen.ecommerce.modules.recommendation.repository.AssociationRuleRebuildLogRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssociationRuleRebuildAuditService {

    private static final int MAX_ERROR_LENGTH = 4000;

    private final AssociationRuleRebuildLogRepository repository;

    public AssociationRuleRebuildAuditService(AssociationRuleRebuildLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UUID createRunning(
            String modelVersion,
            double minSupport,
            double minConfidence,
            double minLift,
            Instant startedAt) {
        AssociationRuleRebuildLog log = new AssociationRuleRebuildLog();
        log.setModelVersion(modelVersion);
        log.setStatus(RebuildStatus.RUNNING);
        log.setMinSupport(minSupport);
        log.setMinConfidence(minConfidence);
        log.setMinLift(minLift);
        log.setStartedAt(startedAt);
        return repository.saveAndFlush(log).getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(UUID logId, RuntimeException failure) {
        repository.findById(logId).ifPresent(log -> {
            String message = failure.getMessage() == null
                    ? failure.getClass().getSimpleName()
                    : failure.getMessage();
            log.setStatus(RebuildStatus.FAILED);
            log.setFinishedAt(Instant.now());
            log.setErrorMessage(message.substring(0, Math.min(message.length(), MAX_ERROR_LENGTH)));
            repository.saveAndFlush(log);
        });
    }
}
