package org.ngs.payments.repository;

import org.ngs.payments.entity.PaymentOrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentOrderItemRepository extends JpaRepository<PaymentOrderItemEntity, Long> {
    List<PaymentOrderItemEntity> findByPaymentOrderId(Long paymentOrderId);
}
