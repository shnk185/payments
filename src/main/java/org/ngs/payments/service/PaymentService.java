package org.ngs.payments.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.ngs.payments.dto.external.response.PaymentSessionCreateResponse;
import org.ngs.payments.dto.response.MakePaymentResponse;
import org.ngs.payments.entity.PaymentEntity;
import org.ngs.payments.enums.PaymentStatus;
import org.ngs.payments.exception.InvalidRequestException;
import org.ngs.payments.exception.ResourceNotFoundException;
import org.ngs.payments.repository.PaymentRepository;
import org.ngs.payments.service.external.PaymentProviderService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentProviderService paymentProviderService;
    private final PaymentRepository paymentRepository;

    @Transactional
    public MakePaymentResponse makePayment(String paymentUniqueId, long userId) {
        PaymentEntity paymentEntity = paymentRepository.findByUniqueIdAndBuyerId(paymentUniqueId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid payment id"));

        if (paymentEntity.getStatus() == PaymentStatus.SUCCESS) {
            throw new InvalidRequestException("Payment already completed");
        }
        if (paymentEntity.getCheckoutUrl() != null) {
            return MakePaymentResponse.builder().url(paymentEntity.getCheckoutUrl()).build();
        }

        Map<String, String> metadata = new HashMap<>();
        metadata.put("paymentUUID", paymentEntity.getUniqueId());

        PaymentSessionCreateResponse paymentSessionCreateResponse = paymentProviderService.createPaymentSession(
                paymentEntity.getAmount(), paymentEntity.getCurrency(), "", "", metadata, paymentUniqueId);

        paymentEntity.setCheckoutSessionId(paymentSessionCreateResponse.getId());
        paymentEntity.setCheckoutUrl(paymentSessionCreateResponse.getUrl());
        paymentEntity.setStatus(PaymentStatus.EXECUTING);
        paymentRepository.save(paymentEntity);

        return MakePaymentResponse.builder().url(paymentSessionCreateResponse.getUrl()).build();
    }
}
