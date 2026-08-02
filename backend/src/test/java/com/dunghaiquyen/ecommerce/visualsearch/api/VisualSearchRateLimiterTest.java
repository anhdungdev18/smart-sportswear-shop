package com.dunghaiquyen.ecommerce.visualsearch.api;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

class VisualSearchRateLimiterTest {

    @Test
    void limitsEachStableClientIdentityIndependently() {
        VisualSearchRateLimiter limiter = new VisualSearchRateLimiter(
                new VisualSearchProperties(true, "http://localhost", "token", 5, 1));

        limiter.check("session:first");
        limiter.check("session:second");

        assertThatThrownBy(() -> limiter.check("session:first"))
                .isInstanceOf(BusinessRuleException.class);
    }
}
