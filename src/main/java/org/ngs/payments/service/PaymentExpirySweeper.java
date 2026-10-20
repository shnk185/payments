package org.ngs.payments.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ngs.payments.entity.PaymentEntity;
import org.ngs.payments.entity.PaymentOrderEntity;
import org.ngs.payments.enums.PaymentStatus;
import org.ngs.payments.repository.PaymentOrderRepository;
import org.ngs.payments.repository.PaymentRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class PaymentExpirySweeper {
    private static final Duration GRACE = Duration.ofMinutes(5);

    private final PaymentRepository paymentRepository;
    private final PaymentOrderRepository paymentOrderRepository;

    @Scheduled(fixedDelayString = "${payments.expiry.sweep-interval-ms:60000}")
    @Transactional
    public void expireStalePayments() {
        List<PaymentEntity> payments = paymentRepository.findByStatusAndExpiresAtBefore(
                PaymentStatus.EXECUTING, Instant.now().minus(GRACE));
        if (payments.isEmpty()) {
            return;
        }

        for (PaymentEntity payment : payments) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setLastErrorCode("payment_expired");
            payment.setLastErrorMessage("Payment expired before completion.");

            List<PaymentOrderEntity> orders = paymentOrderRepository.findByPaymentId(payment.getId());
            orders.forEach(order -> order.setStatus(PaymentStatus.FAILED));
            paymentOrderRepository.saveAll(orders);
        }
        paymentRepository.saveAll(payments);
        log.info("expired {} stale payments", payments.size());
    }
}
