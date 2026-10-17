package org.ngs.payments.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ngs.payments.enums.Currency;
import org.ngs.payments.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEntity extends BaseEntity {
    @Column(nullable = false)
    private Long cartId;

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false, unique = true)
    private String uniqueId;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
