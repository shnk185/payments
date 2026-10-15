package org.ngs.payments.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.ngs.payments.enums.Currency;

import java.math.BigDecimal;

@Entity
@Table(name = "items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemEntity extends BaseEntity {
    private String name;
    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal cost;
    private Currency currency;
    private Long sellerId;
}
