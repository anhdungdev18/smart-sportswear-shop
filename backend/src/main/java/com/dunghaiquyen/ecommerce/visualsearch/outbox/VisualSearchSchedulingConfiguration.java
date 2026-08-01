package com.dunghaiquyen.ecommerce.visualsearch.outbox;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "app.visual-search", name = "enabled", havingValue = "true")
public class VisualSearchSchedulingConfiguration {
}
