package com.dunghaiquyen.ecommerce.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * No real shipping engine exists this phase - a flat fee plus a free-shipping
 * subtotal threshold, both configurable without a redeploy. Defaults (0/0)
 * preserve the exact pre-existing behavior (shippingFee always 0) until an
 * operator explicitly opts in - see OrderService.calculateShippingFee.
 */
@ConfigurationProperties(prefix = "app.shipping")
public record AppShippingProperties(BigDecimal flatFee, BigDecimal freeShippingThreshold) {
}
