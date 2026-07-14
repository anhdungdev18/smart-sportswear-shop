package com.dunghaiquyen.ecommerce.modules.recommendation.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import com.dunghaiquyen.ecommerce.modules.product.entity.Product;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RebuildAssociationRulesRequest;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RebuildAssociationRulesResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.AssociationRule;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.AssociationRuleRebuildLog;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.AssociationRuleStatus;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RebuildStatus;
import com.dunghaiquyen.ecommerce.modules.recommendation.repository.AssociationRuleMiningRepository;
import com.dunghaiquyen.ecommerce.modules.recommendation.repository.AssociationRuleRebuildLogRepository;
import com.dunghaiquyen.ecommerce.modules.recommendation.repository.AssociationRuleRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssociationRuleMiningService {

    private static final double DEFAULT_MIN_SUPPORT = 0.01;
    private static final double DEFAULT_MIN_CONFIDENCE = 0.10;
    private static final double DEFAULT_MIN_LIFT = 1.0;
    private static final int DEFAULT_MIN_TRANSACTIONS = 2;

    private static final List<OrderStatus> TRAINING_ORDER_STATUSES = List.of(
            OrderStatus.DELIVERED
    );

    private final AssociationRuleMiningRepository miningRepository;
    private final AssociationRuleRepository associationRuleRepository;
    private final AssociationRuleRebuildLogRepository rebuildLogRepository;
    private final AssociationRuleRebuildAuditService auditService;
    private final EntityManager entityManager;

    public AssociationRuleMiningService(
            AssociationRuleMiningRepository miningRepository,
            AssociationRuleRepository associationRuleRepository,
            AssociationRuleRebuildLogRepository rebuildLogRepository,
            AssociationRuleRebuildAuditService auditService,
            EntityManager entityManager) {
        this.miningRepository = miningRepository;
        this.associationRuleRepository = associationRuleRepository;
        this.rebuildLogRepository = rebuildLogRepository;
        this.auditService = auditService;
        this.entityManager = entityManager;
    }

    @Transactional
    public RebuildAssociationRulesResponse rebuildAssociationRules(RebuildAssociationRulesRequest request) {
        MiningConfig config = resolveConfig(request);
        miningRepository.acquireRebuildLock();

        Instant startedAt = Instant.now();
        String modelVersion = "assoc-" + UUID.randomUUID();
        UUID logId = auditService.createRunning(
                modelVersion,
                config.minSupport(),
                config.minConfidence(),
                config.minLift(),
                startedAt
        );

        try {
            return rebuildRules(config, startedAt, modelVersion, logId);
        } catch (RuntimeException ex) {
            auditService.markFailed(logId, ex);
            throw ex;
        }
    }

    private RebuildAssociationRulesResponse rebuildRules(
            MiningConfig config,
            Instant startedAt,
            String modelVersion,
            UUID logId) {

        List<AssociationRuleMiningRepository.OrderProductRow> rows =
                miningRepository.findOrderProductRowsForTraining(TRAINING_ORDER_STATUSES);

        List<Set<UUID>> baskets = buildBaskets(rows);
        long totalTransactions = baskets.size();

        List<AssociationRule> newRules = buildAssociationRules(baskets, modelVersion, config);

        /*
         * An toàn cho demo:
         * Chỉ archive rule cũ khi lần train mới sinh ra ít nhất 1 rule.
         * Nếu DB chưa đủ dữ liệu hoặc threshold quá cao khiến không có rule,
         * mình không xóa rule ACTIVE cũ để tránh làm recommendation bị rỗng.
         */
        if (!newRules.isEmpty()) {
            associationRuleRepository.archiveActiveRules();
            associationRuleRepository.saveAll(newRules);
        }

        Instant finishedAt = Instant.now();
        AssociationRuleRebuildLog log = rebuildLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalStateException("Rebuild audit log not found"));
        log.setStatus(RebuildStatus.SUCCESS);
        log.setTotalTransactions(totalTransactions);
        log.setTotalRules(newRules.size());
        log.setFinishedAt(finishedAt);
        rebuildLogRepository.save(log);

        String message = newRules.isEmpty()
                ? "Rebuild completed but no association rules were generated"
                : "Rebuild completed successfully";

        return new RebuildAssociationRulesResponse(
                modelVersion,
                RebuildStatus.SUCCESS,
                totalTransactions,
                newRules.size(),
                config.minSupport(),
                config.minConfidence(),
                config.minLift(),
                startedAt,
                finishedAt,
                message
        );
    }

    private List<Set<UUID>> buildBaskets(List<AssociationRuleMiningRepository.OrderProductRow> rows) {
        Map<UUID, Set<UUID>> basketByOrderId = new LinkedHashMap<>();

        for (AssociationRuleMiningRepository.OrderProductRow row : rows) {
            basketByOrderId
                    .computeIfAbsent(row.orderId(), ignored -> new HashSet<>())
                    .add(row.productId());
        }

        return List.copyOf(basketByOrderId.values());
    }

    private List<AssociationRule> buildAssociationRules(
            List<Set<UUID>> baskets,
            String modelVersion,
            MiningConfig config) {
        if (baskets.size() < config.minTransactions()) {
            return List.of();
        }

        long totalTransactions = baskets.size();

        Map<UUID, Long> productCounts = new HashMap<>();
        Map<ProductPair, Long> pairCounts = new HashMap<>();

        for (Set<UUID> basket : baskets) {
            List<UUID> products = basket.stream()
                    .sorted()
                    .toList();

            for (UUID productId : products) {
                productCounts.merge(productId, 1L, Long::sum);
            }

            for (int i = 0; i < products.size(); i++) {
                for (int j = i + 1; j < products.size(); j++) {
                    ProductPair pair = ProductPair.of(products.get(i), products.get(j));
                    pairCounts.merge(pair, 1L, Long::sum);
                }
            }
        }

        List<AssociationRule> rules = new ArrayList<>();

        for (Map.Entry<ProductPair, Long> entry : pairCounts.entrySet()) {
            ProductPair pair = entry.getKey();
            long pairCount = entry.getValue();

            addRuleIfQualified(
                    rules,
                    pair.first(),
                    pair.second(),
                    pairCount,
                    productCounts,
                    totalTransactions,
                    modelVersion,
                    config
            );

            addRuleIfQualified(
                    rules,
                    pair.second(),
                    pair.first(),
                    pairCount,
                    productCounts,
                    totalTransactions,
                    modelVersion,
                    config
            );
        }

        rules.sort(Comparator
                .comparing(AssociationRule::getConfidence).reversed()
                .thenComparing(AssociationRule::getLift, Comparator.reverseOrder())
                .thenComparing(AssociationRule::getSupport, Comparator.reverseOrder()));

        return rules;
    }

    private void addRuleIfQualified(
            List<AssociationRule> rules,
            UUID antecedentProductId,
            UUID consequentProductId,
            long pairCount,
            Map<UUID, Long> productCounts,
            long totalTransactions,
            String modelVersion,
            MiningConfig config) {
        long antecedentCount = productCounts.getOrDefault(antecedentProductId, 0L);
        long consequentCount = productCounts.getOrDefault(consequentProductId, 0L);

        if (antecedentCount == 0 || consequentCount == 0 || totalTransactions == 0) {
            return;
        }

        double support = (double) pairCount / totalTransactions;
        double confidence = (double) pairCount / antecedentCount;
        double consequentProbability = (double) consequentCount / totalTransactions;
        double lift = confidence / consequentProbability;

        if (support < config.minSupport()) {
            return;
        }

        if (confidence < config.minConfidence()) {
            return;
        }

        if (lift < config.minLift()) {
            return;
        }

        AssociationRule rule = new AssociationRule();
        rule.setAntecedentProduct(entityManager.getReference(Product.class, antecedentProductId));
        rule.setConsequentProduct(entityManager.getReference(Product.class, consequentProductId));
        rule.setSupport(support);
        rule.setConfidence(confidence);
        rule.setLift(lift);
        rule.setAntecedentCount(antecedentCount);
        rule.setConsequentCount(consequentCount);
        rule.setPairCount(pairCount);
        rule.setTotalTransactions(totalTransactions);
        rule.setModelVersion(modelVersion);
        rule.setStatus(AssociationRuleStatus.ACTIVE);

        rules.add(rule);
    }

    private MiningConfig resolveConfig(RebuildAssociationRulesRequest request) {
        double minSupport = resolveRatio(
                "minSupport",
                request != null ? request.minSupport() : null,
                DEFAULT_MIN_SUPPORT
        );

        double minConfidence = resolveRatio(
                "minConfidence",
                request != null ? request.minConfidence() : null,
                DEFAULT_MIN_CONFIDENCE
        );

        double minLift = resolveNonNegative(
                "minLift",
                request != null ? request.minLift() : null,
                DEFAULT_MIN_LIFT
        );

        int minTransactions = request != null && request.minTransactions() != null
                ? request.minTransactions()
                : DEFAULT_MIN_TRANSACTIONS;

        if (minTransactions < 2) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, "minTransactions must be greater than or equal to 2");
        }

        return new MiningConfig(minSupport, minConfidence, minLift, minTransactions);
    }

    private double resolveRatio(String fieldName, Double value, double defaultValue) {
        double resolved = value != null ? value : defaultValue;

        if (resolved < 0 || resolved > 1) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, fieldName + " must be between 0 and 1");
        }

        return resolved;
    }

    private double resolveNonNegative(String fieldName, Double value, double defaultValue) {
        double resolved = value != null ? value : defaultValue;

        if (resolved < 0) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, fieldName + " must be greater than or equal to 0");
        }

        return resolved;
    }

    private record MiningConfig(
            double minSupport,
            double minConfidence,
            double minLift,
            int minTransactions
    ) {
    }

    private record ProductPair(UUID first, UUID second) {

        private static ProductPair of(UUID productA, UUID productB) {
            if (productA.equals(productB)) {
                throw new IllegalArgumentException("Product pair must contain two different products");
            }

            return productA.compareTo(productB) < 0
                    ? new ProductPair(productA, productB)
                    : new ProductPair(productB, productA);
        }
    }
}
