package org.ngs.payments.repository;

import org.ngs.payments.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByIdempotencyKey(String idempotencyKey);
    Optional<PaymentEntity> findByUniqueIdAndBuyerId(String uniqueId, Long buyerId);
    Optional<PaymentEntity> findByCheckoutSessionId(String checkoutSessionId);
}
