package com.dunghaiquyen.ecommerce.modules.replenishment.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
@ConditionalOnProperty(prefix = "app.forecast-demo", name = "enabled", havingValue = "true")
public class ForecastDemoDataRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ForecastDemoDataRunner.class);
    private final ForecastDemoDataSeeder seeder;

    public ForecastDemoDataRunner(ForecastDemoDataSeeder seeder) {
        this.seeder = seeder;
    }

    @Override
    public void run(String... args) {
        ForecastDemoDataSeeder.SeedSummary summary = seeder.seed();
        log.info("Forecast demo seed complete: variants={}, orders={}, historyDays={}, marker={}",
                summary.variants(), summary.orders(), summary.historyDays(), ForecastDemoDataSeeder.ORDER_MARKER);
    }
}
