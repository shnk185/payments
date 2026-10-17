package org.ngs.payments.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ngs.payments.enums.Currency;
import org.ngs.payments.enums.PaymentStatus;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderEntity extends BaseEntity {
    @Column(nullable = false)
    private Long paymentId;

    @Column(nullable = false)
    private Long sellerId;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal subtotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;
}
