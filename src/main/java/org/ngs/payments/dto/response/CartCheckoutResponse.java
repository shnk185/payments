package org.ngs.payments.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartCheckoutResponse {
    private String uniqueId;
    private String paymentUUID;
    private BigDecimal cartCost;
}
