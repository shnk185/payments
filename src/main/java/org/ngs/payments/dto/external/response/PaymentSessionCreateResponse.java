package org.ngs.payments.dto.external.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ngs.payments.enums.Currency;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSessionCreateResponse {
    private String id;
    private String url;
    private String status;
    private BigDecimal amount;
    private Currency currency;
    private Instant expiresAt;
    private String successUrl;
    private String cancelUrl;
}
