package org.ngs.payments.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ngs.payments.dto.CartItem;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartCheckoutResponse {
    private String uniqueId;
    private String paymentUUID;
    private BigDecimal cartCost;
    private List<CartItem> cartItems;
}
