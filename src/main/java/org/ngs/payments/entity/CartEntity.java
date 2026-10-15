package org.ngs.payments.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "carts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartEntity extends BaseEntity {
    private String uniqueId;
    private Long userId;
}
