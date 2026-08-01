package com.dunghaiquyen.ecommerce.visualsearch.api;

import com.fasterxml.jackson.annotation.JsonAlias;

public record VisualSearchOperationsResponse(
        String provider,
        String model,
        Integer dimensions,
        @JsonAlias("outbox_pending") int outboxPending,
        @JsonAlias("outbox_publishing") int outboxPublishing,
        @JsonAlias("outbox_failed") int outboxFailed,
        @JsonAlias("rabbitmq_available") boolean rabbitmqAvailable,
        @JsonAlias("main_queue_messages") Integer mainQueueMessages,
        @JsonAlias("retry_queue_messages") Integer retryQueueMessages,
        @JsonAlias("dlq_messages") Integer dlqMessages,
        @JsonAlias("monthly_cost_usd") double monthlyCostUsd,
        @JsonAlias("monthly_budget_usd") double monthlyBudgetUsd,
        @JsonAlias("budget_usage_pct") double budgetUsagePct,
        @JsonAlias("budget_exhausted") boolean budgetExhausted) {
}
