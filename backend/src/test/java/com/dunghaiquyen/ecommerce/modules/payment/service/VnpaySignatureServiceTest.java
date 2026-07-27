package com.dunghaiquyen.ecommerce.modules.payment.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.dunghaiquyen.ecommerce.config.AppVnpayProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class VnpaySignatureServiceTest {

    private final VnpaySignatureService service = new VnpaySignatureService(
            new AppVnpayProperties("TESTTMN1", "secret", "https://pay", "https://return", "https://callback"));

    @Test
    void canonicalDataIsSortedAndUrlEncoded() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_OrderInfo", "Thanh toan don hang: 1");
        params.put("vnp_Amount", "100000");

        assertThat(service.buildQueryString(params))
                .isEqualTo("vnp_Amount=100000&vnp_OrderInfo=Thanh+toan+don+hang%3A+1");
    }

    @Test
    void verificationRejectsAnyPayloadMutation() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_TxnRef", "TXN1");
        params.put("vnp_Amount", "100000");
        params.put("vnp_SecureHash", service.hash(params));
        assertThat(service.verify(params)).isTrue();

        params.put("vnp_Amount", "200000");
        assertThat(service.verify(params)).isFalse();
    }
}
