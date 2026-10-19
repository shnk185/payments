package org.ngs.payments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ngs.payments.dto.response.MakePaymentResponse;
import org.ngs.payments.service.PaymentService;
import org.ngs.payments.util.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentsController {
    private final PaymentService paymentService;

    @PutMapping("/{paymentUniqueId}")
    public ResponseEntity<MakePaymentResponse> makePayment(@PathVariable String paymentUniqueId) {
        long userId = SecurityUtils.currentUserId();
        log.info("received payment request {} {}", paymentUniqueId, userId);
        MakePaymentResponse response = paymentService.makePayment(paymentUniqueId, userId);
        return ResponseEntity.ok(response);
    }
}
