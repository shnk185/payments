package org.ngs.payments.controller;

import lombok.RequiredArgsConstructor;
import org.ngs.payments.service.WebhookService;
import org.ngs.payments.service.WebhookSignatureVerifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class PspWebhookController {
    private final WebhookSignatureVerifier webhookSignatureVerifier;
    private final WebhookService webhookService;

    @PostMapping("/psp")
    public ResponseEntity<Void> receivePspWebhook(@RequestHeader(value = "Psp-Signature", required = false) String signature,
                                                  @RequestBody byte[] payload) {
        webhookSignatureVerifier.verify(signature, payload);
        webhookService.process(payload);
        return ResponseEntity.ok().build();
    }
}
