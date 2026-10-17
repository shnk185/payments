package org.ngs.payments.repository;

import org.ngs.payments.entity.PaymentOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrderEntity, Long> {
    List<PaymentOrderEntity> findByPaymentId(Long paymentId);
}
