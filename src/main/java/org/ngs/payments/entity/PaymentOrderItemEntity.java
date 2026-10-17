package org.ngs.payments.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ngs.payments.enums.Currency;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_order_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderItemEntity extends BaseEntity {
    @Column(nullable = false)
    private Long paymentOrderId;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private String name;

    @Column(name = "cost_per_unit", precision = 19, scale = 4, nullable = false)
    private BigDecimal costPerUnit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Currency currency;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "line_total", precision = 19, scale = 4, nullable = false)
    private BigDecimal lineTotal;
}
