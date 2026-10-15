package org.ngs.payments.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEntity extends BaseEntity {
    private Long itemId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
}
