package org.ngs.payments.service;

import lombok.RequiredArgsConstructor;
import org.ngs.payments.config.properties.WebhookConfig;
import org.ngs.payments.exception.InvalidWebhookSignatureException;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class WebhookSignatureVerifier {
    private final WebhookConfig webhookConfig;

    public void verify(String header, byte[] payload) {
        if (header == null || header.isBlank()) {
            throw new InvalidWebhookSignatureException("Missing signature header");
        }
        String timestamp = null;
        String signature = null;
        for (String part : header.split(",")) {
            String[] pair = part.trim().split("=", 2);
            if (pair.length != 2) {
                continue;
            }
            if ("t".equals(pair[0])) {
                timestamp = pair[1];
            }
            if ("v1".equals(pair[0])) {
                signature = pair[1];
            }
        }
        if (timestamp == null || signature == null) {
            throw new InvalidWebhookSignatureException("Malformed signature header");
        }

        long sentAt;
        try {
            sentAt = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            throw new InvalidWebhookSignatureException("Malformed signature timestamp");
        }
        if (Math.abs(Instant.now().getEpochSecond() - sentAt) > webhookConfig.getToleranceSeconds()) {
            throw new InvalidWebhookSignatureException("Webhook timestamp outside tolerance");
        }

        byte[] received;
        try {
            received = HexFormat.of().parseHex(signature);
        } catch (IllegalArgumentException e) {
            throw new InvalidWebhookSignatureException("Malformed signature");
        }
        if (!MessageDigest.isEqual(hmac(timestamp, payload), received)) {
            throw new InvalidWebhookSignatureException("Invalid webhook signature");
        }
    }

    private byte[] hmac(String timestamp, byte[] payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookConfig.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            mac.update(timestamp.getBytes(StandardCharsets.UTF_8));
            mac.update((byte) '.');
            mac.update(payload);
            return mac.doFinal();
        } catch (Exception e) {
            throw new IllegalStateException("could not compute webhook signature", e);
        }
    }
}
