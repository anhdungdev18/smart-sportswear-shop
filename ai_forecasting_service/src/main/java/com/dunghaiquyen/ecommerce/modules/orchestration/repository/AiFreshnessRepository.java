package com.dunghaiquyen.ecommerce.modules.orchestration.repository;

import java.time.Instant;
import java.time.LocalDate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class AiFreshnessRepository {

    private final JdbcClient jdbcClient;

    public AiFreshnessRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public LocalDate latestSalesDate(String dataSource) {
        return jdbcClient.sql("select max(sales_date) from ai_sales_daily_snapshot where data_source = :dataSource")
                .param("dataSource", dataSource)
                .query(LocalDate.class)
                .optional()
                .orElse(null);
    }

    public long salesRows(String dataSource) {
        return count("ai_sales_daily_snapshot", "data_source", dataSource);
    }

    public long forecastRows(String dataSource) {
        return count("forecast_runs", "data_source", dataSource);
    }

    public long evaluationRows(String dataSource) {
        return count("forecast_model_evaluations", "data_source", dataSource);
    }

    public long pendingRecommendations(String dataSource) {
        return jdbcClient.sql("""
                select count(*)
                from replenishment_recommendations r
                join forecast_runs f on f.id = r.forecast_run_id
                where r.status = 'PENDING'
                  and f.data_source = :dataSource
                """)
                .param("dataSource", dataSource)
                .query(Long.class)
                .single();
    }

    public Instant latestForecastGeneratedAt(String dataSource) {
        return jdbcClient.sql("select max(generated_at) from forecast_runs where data_source = :dataSource")
                .param("dataSource", dataSource)
                .query(Instant.class)
                .optional()
                .orElse(null);
    }

    public Instant latestEvaluationUpdatedAt(String dataSource) {
        return latestInstant("forecast_model_evaluations", "last_evaluated_at", dataSource);
    }

    public Instant latestRecommendationCreatedAt(String dataSource) {
        return jdbcClient.sql("""
                select max(r.created_at)
                from replenishment_recommendations r
                join forecast_runs f on f.id = r.forecast_run_id
                where f.data_source = :dataSource
                """)
                .param("dataSource", dataSource)
                .query(Instant.class)
                .optional()
                .orElse(null);
    }

    private long count(String table, String column, String dataSource) {
        return jdbcClient.sql("select count(*) from " + table + " where " + column + " = :dataSource")
                .param("dataSource", dataSource)
                .query(Long.class)
                .single();
    }

    private Instant latestInstant(String table, String column, String dataSource) {
        return jdbcClient.sql("select max(" + column + ") from " + table + " where data_source = :dataSource")
                .param("dataSource", dataSource)
                .query(Instant.class)
                .optional()
                .orElse(null);
    }
}
