package org.ngs.payments.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ngs.payments.dto.webhook.PspWebhookEvent;
import org.ngs.payments.dto.webhook.PspWebhookSessionData;
import org.ngs.payments.entity.PaymentEntity;
import org.ngs.payments.entity.PaymentOrderEntity;
import org.ngs.payments.enums.PaymentStatus;
import org.ngs.payments.repository.PaymentOrderRepository;
import org.ngs.payments.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {
    private static final String SESSION_COMPLETED = "checkout.session.completed";
    private static final String SESSION_PAYMENT_FAILED = "checkout.session.payment_failed";

    private final PaymentRepository paymentRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void process(byte[] payload) {
        PspWebhookEvent event = objectMapper.readValue(payload, PspWebhookEvent.class);
        if (!SESSION_COMPLETED.equals(event.getType()) && !SESSION_PAYMENT_FAILED.equals(event.getType())) {
            return;
        }

        PspWebhookSessionData data = event.getData();
        PaymentEntity payment = paymentRepository.findByCheckoutSessionId(data.getId()).orElse(null);
        if (payment == null) {
            log.warn("webhook {} for unknown checkout session {}", event.getId(), data.getId());
            return;
        }
        if (data.getAmount() == null || payment.getAmount().compareTo(data.getAmount()) != 0
                || !payment.getCurrency().name().equals(data.getCurrency())) {
            log.error("webhook {} does not match payment {}", event.getId(), payment.getUniqueId());
            return;
        }
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return;
        }

        if (SESSION_PAYMENT_FAILED.equals(event.getType())) {
            payment.setLastErrorCode(data.getLastErrorCode());
            payment.setLastErrorMessage(data.getLastErrorMessage());
            paymentRepository.save(payment);
            return;
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setLastErrorCode(null);
        payment.setLastErrorMessage(null);
        paymentRepository.save(payment);

        List<PaymentOrderEntity> orders = paymentOrderRepository.findByPaymentId(payment.getId());
        orders.forEach(order -> order.setStatus(PaymentStatus.SUCCESS));
        paymentOrderRepository.saveAll(orders);
    }
}
