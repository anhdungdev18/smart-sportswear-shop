package com.dunghaiquyen.ecommerce.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Runs {@code flyway repair} before {@code migrate} on startup.
 *
 * <p>This database was originally migrated by a since-reverted branch that used
 * the same version numbers (V13–V15) for different migrations than the ones now
 * on this branch. Repair rewrites those schema-history rows (checksum,
 * description, script) to match the migrations actually present here so
 * validation passes; migrate then applies the genuinely pending ones. As a
 * bonus it absorbs CRLF/LF checksum drift on Windows checkouts.
 */
@Configuration
public class FlywayConfig {

    @Bean
    FlywayMigrationStrategy repairThenMigrate() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
