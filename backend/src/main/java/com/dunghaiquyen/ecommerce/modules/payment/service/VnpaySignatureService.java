package com.dunghaiquyen.ecommerce.modules.payment.service;

import com.dunghaiquyen.ecommerce.config.AppVnpayProperties;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

/** VNPay 2.1.0 HMAC-SHA512 signing and verification. */
@Service
public class VnpaySignatureService {

    private final AppVnpayProperties properties;

    public VnpaySignatureService(AppVnpayProperties properties) {
        this.properties = properties;
    }

    /** Hashes the sorted URL-encoded key/value representation required by VNPay 2.1.0. */
    public String hash(Map<String, String> params) {
        return hmacSha512(properties.hashSecret(), buildHashData(params));
    }

    /**
     * True only if vnp_SecureHash is present and matches the hash recomputed over
     * every OTHER param. Constant-time compare to avoid a timing side-channel.
     */
    public boolean verify(Map<String, String> params) {
        String received = params.get("vnp_SecureHash");
        if (received == null || received.isBlank()) {
            return false;
        }
        Map<String, String> toHash = new HashMap<>(params);
        toHash.remove("vnp_SecureHash");
        toHash.remove("vnp_SecureHashType");
        String expected = hash(toHash);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), received.getBytes(StandardCharsets.UTF_8));
    }

    /** Builds the same canonical encoded representation used for signing. */
    public String buildQueryString(Map<String, String> params) {
        return buildHashData(params);
    }

    private String buildHashData(Map<String, String> params) {
        List<String> keys = sortedNonEmptyKeys(params);
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(encode(key)).append('=').append(encode(params.get(key)));
        }
        return sb.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private List<String> sortedNonEmptyKeys(Map<String, String> params) {
        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                keys.add(entry.getKey());
            }
        }
        keys.sort(String::compareTo);
        return keys;
    }

    private String hmacSha512(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException("Failed to compute VNPay signature", ex);
        }
    }
}
