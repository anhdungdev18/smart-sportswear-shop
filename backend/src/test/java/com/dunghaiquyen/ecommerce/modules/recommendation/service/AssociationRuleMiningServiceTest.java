package com.dunghaiquyen.ecommerce.modules.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dunghaiquyen.ecommerce.modules.product.entity.Product;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RebuildAssociationRulesRequest;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.AssociationRule;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.AssociationRuleRebuildLog;
import com.dunghaiquyen.ecommerce.modules.recommendation.repository.AssociationRuleMiningRepository;
import com.dunghaiquyen.ecommerce.modules.recommendation.repository.AssociationRuleRebuildLogRepository;
import com.dunghaiquyen.ecommerce.modules.recommendation.repository.AssociationRuleRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssociationRuleMiningServiceTest {

    @Mock
    private AssociationRuleMiningRepository miningRepository;
    @Mock
    private AssociationRuleRepository ruleRepository;
    @Mock
    private AssociationRuleRebuildLogRepository logRepository;
    @Mock
    private AssociationRuleRebuildAuditService auditService;
    @Mock
    private EntityManager entityManager;

    private AssociationRuleMiningService service;
    private UUID logId;

    @BeforeEach
    void setUp() {
        service = new AssociationRuleMiningService(
                miningRepository,
                ruleRepository,
                logRepository,
                auditService,
                entityManager
        );
        logId = UUID.randomUUID();
        when(auditService.createRunning(anyString(), any(Double.class), any(Double.class),
                any(Double.class), any(Instant.class))).thenReturn(logId);
    }

    @Test
    void rebuild_usesAllOrdersInSupportDenominator_includingSingleProductOrders() {
        UUID productA = UUID.randomUUID();
        UUID productB = UUID.randomUUID();
        UUID multiItemOrder = UUID.randomUUID();
        UUID singleItemOrder = UUID.randomUUID();

        when(miningRepository.findOrderProductRowsForTraining(any())).thenReturn(List.of(
                new AssociationRuleMiningRepository.OrderProductRow(multiItemOrder, productA),
                new AssociationRuleMiningRepository.OrderProductRow(multiItemOrder, productB),
                new AssociationRuleMiningRepository.OrderProductRow(singleItemOrder, productA)
        ));
        when(entityManager.getReference(any(), any())).thenAnswer(invocation -> {
            Product product = new Product();
            product.setId(invocation.getArgument(1));
            return product;
        });
        when(logRepository.findById(logId)).thenReturn(Optional.of(new AssociationRuleRebuildLog()));

        var response = service.rebuildAssociationRules(
                new RebuildAssociationRulesRequest(0.0, 0.0, 0.0, 2)
        );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<AssociationRule>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(ruleRepository).saveAll(captor.capture());
        List<AssociationRule> rules = ((List<AssociationRule>) captor.getValue());

        assertThat(response.totalTransactions()).isEqualTo(2);
        assertThat(rules).hasSize(2);
        assertThat(rules).allSatisfy(rule -> {
            assertThat(rule.getSupport()).isEqualTo(0.5);
            assertThat(rule.getTotalTransactions()).isEqualTo(2);
        });

        InOrder order = inOrder(miningRepository);
        order.verify(miningRepository).acquireRebuildLock();
        order.verify(miningRepository).findOrderProductRowsForTraining(any());
    }

    @Test
    void rebuild_failure_isPersistedThroughIndependentAuditService() {
        RuntimeException failure = new RuntimeException("training query failed");
        when(miningRepository.findOrderProductRowsForTraining(any())).thenThrow(failure);

        assertThatThrownBy(() -> service.rebuildAssociationRules(null))
                .isSameAs(failure);

        verify(auditService).markFailed(logId, failure);
        verify(ruleRepository, never()).archiveActiveRules();
    }
}
