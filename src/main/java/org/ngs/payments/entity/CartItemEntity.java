package org.ngs.payments.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cart_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cartId", "itemId"}))
public class CartItemEntity extends BaseEntity {
    private Long cartId;
    private Long itemId;
    private Integer selectedCount;
}
