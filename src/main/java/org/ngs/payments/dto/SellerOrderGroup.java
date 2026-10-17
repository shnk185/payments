package org.ngs.payments.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerOrderGroup {
    private Long sellerId;
    private List<CartItem> items;
    private BigDecimal subtotal;
}
