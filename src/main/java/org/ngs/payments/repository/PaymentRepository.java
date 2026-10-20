package org.ngs.payments.repository;

import org.ngs.payments.entity.PaymentEntity;
import org.ngs.payments.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByIdempotencyKey(String idempotencyKey);
    Optional<PaymentEntity> findByUniqueIdAndBuyerId(String uniqueId, Long buyerId);
    Optional<PaymentEntity> findByCheckoutSessionId(String checkoutSessionId);
    List<PaymentEntity> findByStatusAndExpiresAtBefore(PaymentStatus status, Instant cutoff);
}
